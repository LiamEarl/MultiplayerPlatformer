package client.objects;

import client.utility.Vector2D;

import java.util.ArrayList;

/**
 * Level class that stores an arrayList of level game objects and a spawn point
 */
public class Level {
    private ArrayList<GameObject> objects; // ArrayList that stores all GameObjects in the level
    private Vector2D spawnPoint; // Vector that stores the initial spawn point

    /**
     * Create a new Level Object using an arrayList of GameObjects and a Vector.
     * @param objects ArrayList of GameObjects to add to the level.
     * @param spawnPoint Level initial spawn location.
     */
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
