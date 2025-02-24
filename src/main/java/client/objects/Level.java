package client.objects;

import client.utility.Vector2D;

import java.util.ArrayList;
public class Level {
    private ArrayList<GameObject> objects;
    private Vector2D spawnPoint;

    public Level(ArrayList<GameObject> objects, Vector2D spawnPoint) {
        this.objects = objects;
        this.spawnPoint = spawnPoint;
    }

    public ArrayList<GameObject> getObjects() {
        return objects;
    }

    public void setObjects(ArrayList<GameObject> objects) {
        this.objects = objects;
    }

    public Vector2D getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Vector2D spawnPoint) {
        this.spawnPoint = spawnPoint;
    }
}
