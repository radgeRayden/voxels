load-library "noise.dll"

fn (c)
    c.window.title = "voxels!"
    c.window.width = 800
    c.window.height = 600
    c.graphics.backend = "WebGPU"
    c.modules.graphics = false
