using import struct
let window = (import .bottle.src.window)
let wgpu = (import .bottle.src.FFI.wgpu)

inline &local (T ...)
    &
        local T
            ...

struct GfxState plain
    surface : wgpu.Surface
    adapter : wgpu.Adapter
    device  : wgpu.Device
    swapchain : wgpu.SwapChain
    queue : wgpu.Queue

global istate : GfxState

fn create-wgpu-surface ()
    from window let get-native-window-info
    static-match operating-system
    case 'linux
        let x11-display x11-window = (get-native-window-info)
        wgpu.InstanceCreateSurface null
            &local wgpu.SurfaceDescriptor
                nextInChain =
                    as
                        &local wgpu.SurfaceDescriptorFromXlib
                            chain = 
                                wgpu.ChainedStruct
                                    sType = wgpu.SType.SurfaceDescriptorFromXlib
                            display = x11-display
                            window = x11-window
                        mutable@ wgpu.ChainedStruct 
    case 'windows
        let hinstance hwnd = (get-native-window-info)
        wgpu.InstanceCreateSurface null
            &local wgpu.SurfaceDescriptor
                nextInChain =
                    as
                        &local wgpu.SurfaceDescriptorFromWindowsHWND
                            chain = 
                                wgpu.ChainedStruct
                                    sType = wgpu.SType.SurfaceDescriptorFromWindowsHWND
                            hinstance = hinstance
                            hwnd = hwnd
                        mutable@ wgpu.ChainedStruct 
    default
        error "OS not supported"

fn init ()
    istate.surface = (create-wgpu-surface)
    wgpu.InstanceRequestAdapter null
        &local wgpu.RequestAdapterOptions
            compatibleSurface = istate.surface
        fn (result userdata)
            istate.adapter = result
        null
    wgpu.AdapterRequestDevice istate.adapter
        &local wgpu.DeviceDescriptor
        fn (result userdata)
            istate.device = result
        null

    let ww wh = (window.size)
    istate.swapchain =
        wgpu.DeviceCreateSwapChain istate.device istate.surface
            &local wgpu.SwapChainDescriptor
                label = "swapchain"
                usage = wgpu.TextureUsage.RenderAttachment
                format = wgpu.TextureFormat.RGBA8UnormSrgb
                width = (ww as u32)
                height = (wh as u32)
                presentMode = wgpu.PresentMode.Fifo
    istate.queue = (wgpu.DeviceGetQueue istate.device)

fn present ()
    let swapchain-image = (wgpu.SwapChainGetCurrentTextureView istate.swapchain)

    let cmd-encoder =
        wgpu.DeviceCreateCommandEncoder istate.device 
            &local wgpu.CommandEncoderDescriptor
                label = "command encoder"

    let rp =
        wgpu.CommandEncoderBeginRenderPass cmd-encoder
            &local wgpu.RenderPassDescriptor
                label = "output render pass"
                colorAttachmentCount = 1
                colorAttachments = 
                    &local wgpu.RenderPassColorAttachmentDescriptor
                        attachment = swapchain-image
                        clearColor = (typeinit 0.017 0.017 0.017 1.0)

    wgpu.RenderPassEncoderEndPass rp

    local cmd-buffer = 
        wgpu.CommandEncoderFinish cmd-encoder
            &local wgpu.CommandBufferDescriptor
                label = "command buffer"

    wgpu.QueueSubmit istate.queue 1 &cmd-buffer
    wgpu.SwapChainPresent istate.swapchain
    ;

do
    let init present
    locals;
