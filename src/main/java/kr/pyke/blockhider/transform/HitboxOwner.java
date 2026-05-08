package kr.pyke.blockhider.transform;

import net.minecraft.world.entity.player.Player;

public interface HitboxOwner {
    void blockhider$setOwner(Player player);
    Player blockhider$getOwner();
}