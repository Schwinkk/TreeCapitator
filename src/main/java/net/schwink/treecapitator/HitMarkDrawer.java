package net.schwink.treecapitator;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber
public class HitMarkDrawer extends Screen{

    private static final ResourceLocation HIT_ICON = ResourceLocation.fromNamespaceAndPath(TreeCapitator.MODID, "textures/gui/hit_marker.png");
    public static RenderPipeline renderPipeline = null;

    protected HitMarkDrawer(Component p_96550_) {
        super(p_96550_);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){

        if (HIT_ICON == null){
            System.out.println("SOSAL");
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        //guiGraphics.blitSprite(renderPipeline, HIT_ICON, mouseX, mouseY, 100, 100);

        guiGraphics.fill(100, 500, 300, 1000, 0x80FF0000);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event){
        Minecraft.getInstance().setScreen(new HitMarkDrawer(Component.literal("Hit Marker")));
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent event) {
        if (event.phase) {
            Minecraft mc = Minecraft.getInstance();
            PoseStack poseStack = new PoseStack();
            // кастомный рендер с использованием PoseStack и RenderSystem
        }
    }
}
