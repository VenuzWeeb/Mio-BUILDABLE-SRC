//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Primo\Downloads\Deobf\Minecraft-Deobfuscator3000-master\1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package il.dev.mio.mod.modules.combat;

import java.util.HashSet;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import il.dev.mio.Mio;
import il.dev.mio.api.util.plugs.MathUtil;
import net.minecraft.util.EnumFacing;
import java.util.Iterator;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Comparator;
import il.dev.mio.api.util.interact.EntityUtil;
import java.util.Collection;
import java.util.Collections;
import il.dev.mio.api.util.interact.BlockUtil;
import java.util.ArrayList;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.init.Items;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import il.dev.mio.api.event.events.UpdateWalkingPlayerEvent;
import il.dev.mio.mod.ModuleCore;
import il.dev.mio.api.util.world.Timer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import java.util.Set;
import il.dev.mio.api.util.world.InventoryUtil;
import il.dev.mio.mod.gui.clickgui.setting.Setting;
import il.dev.mio.mod.modules.Module;

public class AntiTrap extends Module
{
    public Setting<Rotate> rotate;
    private final Setting<Integer> coolDown;
    private final Setting<InventoryUtil.Switch> switchMode;
    public Setting<Boolean> sortY;
    public static Set<BlockPos> placedPos;
    private final Vec3d[] surroundTargets;
    private int lastHotbarSlot;
    private boolean switchedItem;
    private boolean offhand;
    private final Timer timer;
    
    public AntiTrap() {
        super("NoTrap", "Places a crystal to prevent you getting trapped.", Category.COMBAT, true, false, false);
        this.rotate = (Setting<Rotate>)this.register(new Setting("Rotate", Rotate.NORMAL));
        this.coolDown = (Setting<Integer>)this.register(new Setting("CoolDown", 400, 0, 1000));
        this.switchMode = (Setting<InventoryUtil.Switch>)this.register(new Setting("Switch", InventoryUtil.Switch.NORMAL));
        this.sortY = (Setting<Boolean>)this.register(new Setting("SortY", true));
        this.surroundTargets = new Vec3d[] { new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, -1.0), new Vec3d(1.0, 0.0, -1.0), new Vec3d(1.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, -1.0), new Vec3d(-1.0, 0.0, 1.0), new Vec3d(1.0, 1.0, 0.0), new Vec3d(0.0, 1.0, 1.0), new Vec3d(-1.0, 1.0, 0.0), new Vec3d(0.0, 1.0, -1.0), new Vec3d(1.0, 1.0, -1.0), new Vec3d(1.0, 1.0, 1.0), new Vec3d(-1.0, 1.0, -1.0), new Vec3d(-1.0, 1.0, 1.0) };
        this.lastHotbarSlot = -1;
        this.offhand = false;
        this.timer = new Timer();
    }
    
    @Override
    public void onEnable() {
        if (ModuleCore.fullNullCheck() || !this.timer.passedMs(this.coolDown.getValue())) {
            this.disable();
            return;
        }
        this.lastHotbarSlot = AntiTrap.mc.player.inventory.currentItem;
    }
    
    @Override
    public void onDisable() {
        if (fullNullCheck()) {
            return;
        }
        this.switchItem(true);
    }
    
    @SubscribeEvent
    public void onUpdateWalkingPlayer(final UpdateWalkingPlayerEvent event) {
        if (!ModuleCore.fullNullCheck() && event.getStage() == 0) {
            this.doAntiTrap();
        }
    }
    
    public void doAntiTrap() {
        final boolean offhand = AntiTrap.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
        this.offhand = offhand;
        final boolean bl = offhand;
        if (!this.offhand && InventoryUtil.findHotbarBlock(ItemEndCrystal.class) == -1) {
            this.disable();
            return;
        }
        this.lastHotbarSlot = AntiTrap.mc.player.inventory.currentItem;
        final ArrayList<Vec3d> targets = new ArrayList<Vec3d>();
        Collections.addAll(targets, BlockUtil.convertVec3ds(AntiTrap.mc.player.getPositionVector(), this.surroundTargets));
        final EntityPlayer closestPlayer = EntityUtil.getClosestEnemy(6.0);
        if (closestPlayer != null) {
            targets.sort((vec3d, vec3d2) -> Double.compare(closestPlayer.getDistanceSq(vec3d2.x, vec3d2.y, vec3d2.z), closestPlayer.getDistanceSq(vec3d.x, vec3d.y, vec3d.z)));
            if (this.sortY.getValue()) {
                targets.sort(Comparator.comparingDouble(vec3d -> vec3d.y));
            }
        }
        for (final Vec3d vec3d3 : targets) {
            final BlockPos pos = new BlockPos(vec3d3);
            if (!BlockUtil.canPlaceCrystal(pos)) {
                continue;
            }
            this.placeCrystal(pos);
            this.disable();
            break;
        }
    }
    
    private void placeCrystal(final BlockPos pos) {
        final boolean bl;
        final boolean mainhand = bl = (AntiTrap.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL);
        if (!mainhand && !this.offhand && !this.switchItem(false)) {
            this.disable();
            return;
        }
        final RayTraceResult result = AntiTrap.mc.world.rayTraceBlocks(new Vec3d(AntiTrap.mc.player.posX, AntiTrap.mc.player.posY + AntiTrap.mc.player.getEyeHeight(), AntiTrap.mc.player.posZ), new Vec3d(pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5));
        final EnumFacing facing = (result == null || result.sideHit == null) ? EnumFacing.UP : result.sideHit;
        final float[] angle = MathUtil.calcAngle(AntiTrap.mc.player.getPositionEyes(AntiTrap.mc.getRenderPartialTicks()), new Vec3d((double)(pos.getX() + 0.5f), (double)(pos.getY() - 0.5f), (double)(pos.getZ() + 0.5f)));
        switch (this.rotate.getValue()) {
            case NORMAL: {
                Mio.rotationManager.setPlayerRotations(angle[0], angle[1]);
                break;
            }
            case PACKET: {
                AntiTrap.mc.player.connection.sendPacket((Packet)new CPacketPlayer.Rotation(angle[0], (float)MathHelper.normalizeAngle((int)angle[1], 360), AntiTrap.mc.player.onGround));
                break;
            }
        }
        AntiTrap.placedPos.add(pos);
        AntiTrap.mc.player.connection.sendPacket((Packet)new CPacketPlayerTryUseItemOnBlock(pos, facing, this.offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
        AntiTrap.mc.player.swingArm(EnumHand.MAIN_HAND);
        this.timer.reset();
    }
    
    private boolean switchItem(final boolean back) {
        if (this.offhand) {
            return true;
        }
        final boolean[] value = InventoryUtil.switchItemToItem(back, this.lastHotbarSlot, this.switchedItem, this.switchMode.getValue(), Items.END_CRYSTAL);
        this.switchedItem = value[0];
        return value[1];
    }
    
    static {
        AntiTrap.placedPos = new HashSet<BlockPos>();
    }
    
    public enum Rotate
    {
        NONE, 
        NORMAL, 
        PACKET;
    }
}
