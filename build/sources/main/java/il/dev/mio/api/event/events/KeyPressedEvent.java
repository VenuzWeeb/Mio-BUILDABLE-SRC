// 
// Decompiled by Procyon v0.5.36
// 

package il.dev.mio.api.event.events;

import il.dev.mio.api.event.EventStage;

public class KeyPressedEvent extends EventStage
{
    public boolean info;
    public boolean pressed;
    
    public KeyPressedEvent(final boolean info, final boolean pressed) {
        this.info = info;
        this.pressed = pressed;
    }
}
