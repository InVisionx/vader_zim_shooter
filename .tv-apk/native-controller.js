/* Native Fire TV input presented as the standard browser Gamepad API. */
(function(){
 'use strict';
 var original=navigator.getGamepads?navigator.getGamepads.bind(navigator):function(){return [];};
 var current=null;
 Object.defineProperty(window,'__fireTVPad',{configurable:true,get:function(){return current;},set:function(pad){
  var was=!!current;current=pad;
  if(pad){pad.timestamp=performance.now();pad.index=0;}
  if(was!==!!pad){var event=new Event(pad?'gamepadconnected':'gamepaddisconnected');Object.defineProperty(event,'gamepad',{value:pad||{index:0,connected:false}});window.dispatchEvent(event);}
 }});
 try{Object.defineProperty(navigator,'getGamepads',{configurable:true,value:function(){return current?[current,null,null,null]:original();}});}catch(e){}
 document.documentElement.style.cursor='none';
})();
