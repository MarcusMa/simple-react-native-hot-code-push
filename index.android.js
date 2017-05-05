'use strict';

import React from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  Navigator,
  BackAndroid,
  TouchableOpacity
} from 'react-native';

import RootTabScene from './components/RootTabScene';
import MobileRechargeScene from './components/MobileRechargeScene';
import Icon from 'react-native-vector-icons/FontAwesome';

class WithReactNative extends React.Component {
  
  componentWillMount(){
    console.log("componentWillMount:" + new Date().getTime());
  }

  componentDidMount(){
    console.log("componentDidMount:" + new Date().getTime());
  }

  render() {
    console.log("render start:" + new Date().getTime());
    return (
      <View style={styles.container}>
        <Navigator 
          initialRoute = {{id:'root'}}
          renderScene = {this._renderScene}
          configureScene = {(route) => {
              if(route.sceneConfig){
                  return route.sceneConfig;
              }
              return Navigator.SceneConfigs.FadeAndroid;
          }}
          navigationBar = {
            <Navigator.NavigationBar 
              routeMapper={NavigationBarRouteMapper}
              style={styles.navBar}
            />
          }
          />
      </View>
    );
  }

    _renderScene(route, navigator){
      BackAndroid.addEventListener('hardwareBackPress',()=>{
        if(navigator && navigator.getCurrentRoutes().length > 1){
          navigator.pop();
          return true;
        }
        return false;
      });
      if (route.id === 'root') {
        return <RootTabScene navigator = {navigator} />
      }
      else if (route.id === 'shoujichongzhi'){
          return <MobileRechargeScene navigator = {navigator} />;
      }
      else{

      }
    }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  navBar: {
    backgroundColor: 'black',
  },
  navBarText: {
    fontSize: 20,
    color: 'white',
  },
  navTitleContainer:{
    flex:1,
    alignItems:"center",
    justifyContent:'center'
  },
  navBarTitleText: {
    fontSize:25,
    fontWeight: '500',
  },
  navBarLeftButton: {
    flex:1,
    flexDirection:'column',
    paddingLeft:10,
    width:50,
    justifyContent:"center",
    alignItems:"center"
  },
  navBarButtonText: {
    color: 'white',
  },
});

const NavigationBarRouteMapper = {
  //左边Button
  LeftButton: function(route, navigator, index, navState) {
     if (route.id !== 'shoujichongzhi') {
       return (
         <View style={styles.navBarLeftButton}>
           <Icon name={'home'} size={30} color={'white'} />
         </View>);
     }
     return (
       <TouchableOpacity
         onPress={() => navigator.pop()}
         style={styles.navBarLeftButton} >
          <Icon name={'angle-left'} size = {30} color = {'white'} />
       </TouchableOpacity>
     );
   },
   //右边Button
   RightButton: function(route, navigator, index, navState) {
     return null;
   },
   //标题
   Title: function(route, navigator, index, navState) {
     let title = "";
     if (route.id === 'root'){
       title = "银联钱包";
     }
     else if (route.id === 'shoujichongzhi'){
       title = "手机充值";
     }
     else{
        //do nothing
     }
     return (
       <View style={styles.navTitleContainer}>
          <Text style={[styles.navBarText, styles.navBarTitleText]}>
            {title}
          </Text>
       </View>
     );
   },
 };

AppRegistry.registerComponent('WithReactNative', () => WithReactNative);