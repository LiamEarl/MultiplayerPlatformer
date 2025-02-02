package client.model;

import java.io.Serializable;

public class EntityData implements Serializable {
    private Vector2D pos;
    private int id;
    public EntityData(Vector2D position, int playerId) {
        this.pos = position;
        this.id = playerId;
    }

    public Vector2D getPos() {
        return pos;
    }

    public void setPos(Vector2D pos) {
        this.pos = pos;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
}
