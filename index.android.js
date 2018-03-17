import React, { Component } from 'react';
import {AppRegistry, Text, View, StyleSheet} from 'react-native';

class WithReactNative extends Component {

 componentWillMount(){
    console.log("componentWillMount:" + new Date().getTime());
  }

  componentDidMount(){
    console.log("componentDidMount:" + new Date().getTime());
  }
  
  render() {
    console.log("render start:" + new Date().getTime());
    return (
      <View style={style.container}>
        <Text style={style.h1}>
              Test Hello World
        </Text>
      </View>
    );
  }
}

const style = StyleSheet.create({
    container:{
      backgroundColor:"#FF4040FF"
    },
    h1:{
      fontSize:35,
    },
    h2:{
      fontSize:30,}
});

AppRegistry.registerComponent('WithReactNative', () => WithReactNative);