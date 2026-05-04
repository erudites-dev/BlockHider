package kr.pyke.blockhider.effect;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

import java.util.List;

public class HintEffect {
    private static final int FLIGHT_DURATION = 1;
    private static final int EXPLOSION_COLOR_ARGB = 0xFFFF0000;
    private static final int RGB_MASK = 0xFFFFFF;

    private HintEffect() { }

    public static void spawn(ServerLevel level, double x, double y, double z) {
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        List<FireworkExplosion> explosions = List.of(new FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL, IntList.of(EXPLOSION_COLOR_ARGB & RGB_MASK), IntList.of(), false, false));
        rocket.set(DataComponents.FIREWORKS, new Fireworks(FLIGHT_DURATION, explosions));

        FireworkRocketEntity firework = new FireworkRocketEntity(level, x, y, z, rocket);
        level.addFreshEntity(firework);
    }
}
