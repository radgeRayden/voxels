import .bottle
let noise = (import .noisep)
import .renderer

global ngen : noise.OpenSimplex2S 0

@@ 'on bottle.load
fn ()
    renderer.init;
    ;

@@ 'on bottle.update
fn (dt)
    ;

@@ 'on bottle.draw 
fn ()
    renderer.present;
    ;

bottle.run;
