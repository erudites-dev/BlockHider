package kr.pyke.blockhider.game;

import kr.pyke.blockhider.type.GAME_ROLE;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class PlayerGameData {
    private final UUID uuid;
    private GAME_ROLE role;
    private boolean alive = true;

    public PlayerGameData(UUID uuid, GAME_ROLE role) {
        this.uuid = uuid;
        this.role = role;
    }

    public UUID getUUID() { return this.uuid; }

    public GAME_ROLE getRole() { return this.role; }

    public void setRole(GAME_ROLE role) { this.role = role; }

    public boolean isAlive() { return this.alive; }

    public void setAlive(boolean alive) { this.alive = alive; }
}
