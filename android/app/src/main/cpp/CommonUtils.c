/*-
 * Copyright 2003-2005 Colin Percival
 * All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted providing that the following conditions 
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

#if 0
__FBSDID("$FreeBSD: src/usr.bin/bsdiff/bspatch/bspatch.c,v 1.1 2005/08/06 01:59:06 cperciva Exp $");
#endif

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "RN JNI", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , "RN JNI", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  , "RN JNI", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN  , "RN JNI", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "RN JNI", __VA_ARGS__)

#include <sys/types.h>
#include "bzip2/bzlib.h"
#include <stdlib.h>
#include <err.h>
#include <unistd.h>
#include <fcntl.h>
#include <jni.h>

// bzip2
#include "bzip2/bzlib.c"
#include "bzip2/crctable.c"
#include "bzip2/compress.c"
#include "bzip2/decompress.c"
#include "bzip2/randtable.c"
#include "bzip2/blocksort.c"
#include "bzip2/huffman.c"

static off_t offtin(u_char *buf) {
    off_t y;

	y=buf[7]&0x7F;
	y=y*256;y+=buf[6];
	y=y*256;y+=buf[5];
	y=y*256;y+=buf[4];
	y=y*256;y+=buf[3];
	y=y*256;y+=buf[2];
	y=y*256;y+=buf[1];
	y=y*256;y+=buf[0];

    if (buf[7] & 0x80) y = -y;

    return y;
}

int bspatch_main(int argc, char *argv[]) {
    FILE *f, *cpf, *dpf, *epf;
    BZFILE *cpfbz2, *dpfbz2, *epfbz2;
    int cbz2err, dbz2err, ebz2err;
    int fd;
    ssize_t oldsize, newsize;
    ssize_t bzctrllen, bzdatalen;
    u_char header[32], buf[8];
    u_char *old, *new;
    off_t oldpos, newpos;
    off_t ctrl[3];
    off_t lenread;
    off_t i;

    if (argc != 4) errx(1, "usage: %s oldfile newfile patchfile\n", argv[0]);

    /* Open patch file */
    if ((f = fopen(argv[3], "r")) == NULL)
        err(1, "fopen(%s)", argv[3]);

    /*
    File format:
        0	8	"BSDIFF40"
        8	8	X
        16	8	Y
        24	8	sizeof(newfile)
        32	X	bzip2(control block)
        32+X	Y	bzip2(diff block)
        32+X+Y	???	bzip2(extra block)
    with control block a set of triples (x,y,z) meaning "add x bytes
    from oldfile to x bytes from the diff block; copy y bytes from the
    extra block; seek forwards in oldfile by z bytes".
    */

    /* Read header */
    if (fread(header, 1, 32, f) < 32) {
        if (feof(f))
            errx(1, "Corrupt patch\n");
        err(1, "fread(%s)", argv[3]);
    }

    /* Check for appropriate magic */
    if (memcmp(header, "BSDIFF40", 8) != 0)
        errx(1, "Corrupt patch\n");

    /* Read lengths from header */
    bzctrllen = offtin(header + 8);
    bzdatalen = offtin(header + 16);
    newsize = offtin(header + 24);
    if ((bzctrllen < 0) || (bzdatalen < 0) || (newsize < 0))
        errx(1, "Corrupt patch\n");

    /* Close patch file and re-open it via libbzip2 at the right places */
    if (fclose(f))
        err(1, "fclose(%s)", argv[3]);
    if ((cpf = fopen(argv[3], "r")) == NULL)
        err(1, "fopen(%s)", argv[3]);
    if (fseeko(cpf, 32, SEEK_SET))
        err(1, "fseeko(%s, %lld)", argv[3],
            (long long) 32);
    if ((cpfbz2 = BZ2_bzReadOpen(&cbz2err, cpf, 0, 0, NULL, 0)) == NULL)
        errx(1, "BZ2_bzReadOpen, bz2err = %d", cbz2err);
    if ((dpf = fopen(argv[3], "r")) == NULL)
        err(1, "fopen(%s)", argv[3]);
    if (fseeko(dpf, 32 + bzctrllen, SEEK_SET))
        err(1, "fseeko(%s, %lld)", argv[3],
            (long long) (32 + bzctrllen));
    if ((dpfbz2 = BZ2_bzReadOpen(&dbz2err, dpf, 0, 0, NULL, 0)) == NULL)
        errx(1, "BZ2_bzReadOpen, bz2err = %d", dbz2err);
    if ((epf = fopen(argv[3], "r")) == NULL)
        err(1, "fopen(%s)", argv[3]);
    if (fseeko(epf, 32 + bzctrllen + bzdatalen, SEEK_SET))
        err(1, "fseeko(%s, %lld)", argv[3],
            (long long) (32 + bzctrllen + bzdatalen));
    if ((epfbz2 = BZ2_bzReadOpen(&ebz2err, epf, 0, 0, NULL, 0)) == NULL)
        errx(1, "BZ2_bzReadOpen, bz2err = %d", ebz2err);

    if (((fd = open(argv[1], O_RDONLY, 0)) < 0) ||
        ((oldsize = lseek(fd, 0, SEEK_END)) == -1) ||
        ((old = malloc(oldsize + 1)) == NULL) ||
        (lseek(fd, 0, SEEK_SET) != 0) ||
        (read(fd, old, oldsize) != oldsize) ||
        (close(fd) == -1))
        err(1, "%s", argv[1]);
    if ((new = malloc(newsize + 1)) == NULL) err(1, NULL);

    oldpos = 0;
    newpos = 0;
    while (newpos < newsize) {
        /* Read control data */
        for (i = 0; i <= 2; i++) {
            lenread = BZ2_bzRead(&cbz2err, cpfbz2, buf, 8);
            if ((lenread < 8) || ((cbz2err != BZ_OK) &&
                                  (cbz2err != BZ_STREAM_END)))
                errx(1, "Corrupt patch\n");
            ctrl[i] = offtin(buf);
        };

        /* Sanity-check */
        if (newpos + ctrl[0] > newsize)
            errx(1, "Corrupt patch\n");

        /* Read diff string */
        lenread = BZ2_bzRead(&dbz2err, dpfbz2, new + newpos, ctrl[0]);
        if ((lenread < ctrl[0]) ||
            ((dbz2err != BZ_OK) && (dbz2err != BZ_STREAM_END)))
            errx(1, "Corrupt patch\n");

        /* Add old data to diff string */
        for (i = 0; i < ctrl[0]; i++)
            if ((oldpos + i >= 0) && (oldpos + i < oldsize))
                new[newpos + i] += old[oldpos + i];

        /* Adjust pointers */
        newpos += ctrl[0];
        oldpos += ctrl[0];

        /* Sanity-check */
        if (newpos + ctrl[1] > newsize)
            errx(1, "Corrupt patch\n");

        /* Read extra string */
        lenread = BZ2_bzRead(&ebz2err, epfbz2, new + newpos, ctrl[1]);
        if ((lenread < ctrl[1]) ||
            ((ebz2err != BZ_OK) && (ebz2err != BZ_STREAM_END)))
            errx(1, "Corrupt patch\n");

        /* Adjust pointers */
        newpos += ctrl[1];
        oldpos += ctrl[2];
    };

    /* Clean up the bzip2 reads */
    BZ2_bzReadClose(&cbz2err, cpfbz2);
    BZ2_bzReadClose(&dbz2err, dpfbz2);
    BZ2_bzReadClose(&ebz2err, epfbz2);
    if (fclose(cpf) || fclose(dpf) || fclose(epf))
        err(1, "fclose(%s)", argv[3]);

    /* Write the new file */
    if (((fd = open(argv[2], O_CREAT | O_TRUNC | O_WRONLY, 0666)) < 0) ||
        (write(fd, new, newsize) != newsize) || (close(fd) == -1))
        err(1, "%s", argv[2]);

    free(new);
    free(old);

    return 0;
}

int bspatch_mem(const unsigned char *old_data, ssize_t old_size,
                const unsigned char *patch_data, ssize_t patch_offset,
                ssize_t patch_size,
                unsigned char **new_data, ssize_t *new_size) {
    // Patch data format:
    //   0       8       "BSDIFF40"
    //   8       8       X
    //   16      8       Y
    //   24      8       sizeof(newfile)
    //   32      X       bzip2(control block)
    //   32+X    Y       bzip2(diff block)
    //   32+X+Y  ???     bzip2(extra block)
    // with control block a set of triples (x,y,z) meaning "add x bytes
    // from oldfile to x bytes from the diff block; copy y bytes from the
    // extra block; seek forwards in oldfile by z bytes".
    unsigned char* header = (unsigned char*) patch_data + patch_offset;
    // header = "BSDIFF40";
    if (memcmp(header, "BSDIFF40", 8) != 0) {
        printf("corrupt bsdiff patch file header (magic number)\n");
        return 1;
    }
    ssize_t ctrl_len, data_len;
    ctrl_len = offtin(header+8);
    data_len = offtin(header+16);
    *new_size = offtin(header+24);
    if (ctrl_len < 0 || data_len < 0 || *new_size < 0) {
        printf("corrupt patch file header (data lengths)\n");
        return 1;
    }
    int bzerr;
    bz_stream cstream;
    cstream.next_in = patch_data + patch_offset + 32;
    cstream.avail_in = ctrl_len;
    cstream.bzalloc = NULL;
    cstream.bzfree = NULL;
    cstream.opaque = NULL;
    if ((bzerr = BZ2_bzDecompressInit(&cstream, 0, 0)) != BZ_OK) {
        printf("failed to bzinit control stream (%d)\n", bzerr);
    }
    bz_stream dstream;
    dstream.next_in = patch_data + patch_offset + 32 + ctrl_len;
    dstream.avail_in = data_len;
    dstream.bzalloc = NULL;
    dstream.bzfree = NULL;
    dstream.opaque = NULL;
    if ((bzerr = BZ2_bzDecompressInit(&dstream, 0, 0)) != BZ_OK) {
        printf("failed to bzinit diff stream (%d)\n", bzerr);
    }
    bz_stream estream;
    estream.next_in = patch_data + patch_offset + 32 + ctrl_len + data_len;
    estream.avail_in = patch_size - (patch_offset + 32 + ctrl_len + data_len);
    estream.bzalloc = NULL;
    estream.bzfree = NULL;
    estream.opaque = NULL;
    if ((bzerr = BZ2_bzDecompressInit(&estream, 0, 0)) != BZ_OK) {
        printf("failed to bzinit extra stream (%d)\n", bzerr);
    }
    *new_data = malloc(*new_size);
    if (*new_data == NULL) {
        printf("failed to allocate %ld bytes of memory for output file\n",
               (long)*new_size);
        return 1;
    }
    off_t oldpos = 0, newpos = 0;
    off_t ctrl[3];
    off_t len_read;
    int i;
    unsigned char buf[24];
    while (newpos < *new_size) {
        // Read control data
        if (FillBuffer(buf, 24, &cstream) != 0) {
            printf("error while reading control stream\n");
            return 1;
        }
        ctrl[0] = offtin(buf);
        ctrl[1] = offtin(buf+8);
        ctrl[2] = offtin(buf+16);
        // Sanity check
        if (newpos + ctrl[0] > *new_size) {
            printf("corrupt patch (new file overrun)\n");
            return 1;
        }
        // Read diff string
        if (FillBuffer(*new_data + newpos, ctrl[0], &dstream) != 0) {
            printf("error while reading diff stream\n");
            return 1;
        }
        // Add old data to diff string
        for (i = 0; i < ctrl[0]; ++i) {
            if ((oldpos+i >= 0) && (oldpos+i < old_size)) {
                (*new_data)[newpos+i] += old_data[oldpos+i];
            }
        }
        // Adjust pointers
        newpos += ctrl[0];
        oldpos += ctrl[0];
        // Sanity check
        if (newpos + ctrl[1] > *new_size) {
            printf("corrupt patch (new file overrun)\n");
            return 1;
        }
        // Read extra string
        if (FillBuffer(*new_data + newpos, ctrl[1], &estream) != 0) {
            printf("error while reading extra stream\n");
            return 1;
        }
        // Adjust pointers
        newpos += ctrl[1];
        oldpos += ctrl[2];
    }
    BZ2_bzDecompressEnd(&cstream);
    BZ2_bzDecompressEnd(&dstream);
    BZ2_bzDecompressEnd(&estream);
    return 0;
}

int FillBuffer(unsigned char* buffer, int size, bz_stream* stream) {
    stream->next_out = (char*)buffer;
    stream->avail_out = size;
    while (stream->avail_out > 0) {
        int bzerr = BZ2_bzDecompress(stream);
        if (bzerr != BZ_OK && bzerr != BZ_STREAM_END) {
            printf("bz error %d decompressing\n", bzerr);
            return -1;
        }
        if (stream->avail_out > 0) {
            printf("need %d more bytes\n", stream->avail_out);
        }
    }
    return 0;
}

/*
 * Class:     com_marcus_lib_codepush_util_CommonUtils
 * Method:    patchMem
 * Signature: ([B[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_marcus_lib_codepush_util_CommonUtils_patchMem
        (JNIEnv *env, jclass type, jbyteArray oldData, jbyteArray patchData){

    jsize oldDataLen = (*env)->GetArrayLength(env, oldData);
    jsize patchDataLen = (*env)->GetArrayLength(env, patchData);

    jbyte *old_data = (*env)->GetByteArrayElements(env, oldData, JNI_FALSE);
    jbyte *patch_data = (*env)->GetByteArrayElements(env, patchData, JNI_FALSE);

    char *oldChars = (char *)malloc(oldDataLen*sizeof(char));
    for(int i=0;i<oldDataLen;i++){
        oldChars[i] = old_data[i];
    }

    char *patchChars = (char *)malloc(patchDataLen*sizeof(char));
    for(int i=0;i<patchDataLen;i++){
        patchChars[i] = patch_data[i];
    }

    ssize_t old_size = oldDataLen;
    ssize_t patch_size = patchDataLen;
    size_t patch_offset = 0;
    char *new_data;
    size_t new_size = 0;
    int ret = bspatch_mem(oldChars, old_size, patchChars, patch_offset, patch_size, &new_data,
                          &new_size);
    if (ret != 0) {
        return NULL;
    } else {
        jbyte *by = (jbyte*) new_data;
        jbyteArray jarray = (*env)->NewByteArray(env,new_size);
        (*env)->SetByteArrayRegion(env,jarray, 0, new_size, by);
        return jarray;
    }
};



JNIEXPORT jint JNICALL
Java_com_marcus_lib_codepush_util_CommonUtils_patch(JNIEnv *env, jclass type,
                                                           jstring oldApkPath_,
                                                           jstring newApkPath_,
                                                           jstring patchPath_) {
    const char *oldApkPath = (*env)->GetStringUTFChars(env, oldApkPath_, 0);
    const char *newApkPath = (*env)->GetStringUTFChars(env, newApkPath_, 0);
    const char *patchPath = (*env)->GetStringUTFChars(env, patchPath_, 0);

    // TODO
    int argc = 4;
    char *argv[4];
    argv[0] = "bspatch";
    argv[1] = oldApkPath;
    argv[2] = newApkPath;
    argv[3] = patchPath;

    int ret = bspatch_main(argc, argv);

    (*env)->ReleaseStringUTFChars(env, oldApkPath_, oldApkPath);
    (*env)->ReleaseStringUTFChars(env, newApkPath_, newApkPath);
    (*env)->ReleaseStringUTFChars(env, patchPath_, patchPath);

    return ret;
}
