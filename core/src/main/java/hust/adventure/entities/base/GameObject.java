package hust.adventure.entities.base;

/**
 * Highest abstract class for all game objects.
 * Contains base identity information of the object: ID, display name, and sprite path.
 */
public abstract class GameObject {
    protected String id;
    protected String name;
    protected String spritePath;

    /**
     * Default constructor for GameObject.
     */
    public GameObject() {
    }

    /**
     * Constructs a GameObject with full identity information.
     *
     * @param id         unique identifier
     * @param name       display name
     * @param spritePath resource path to the image asset (sprite)
     */
    public GameObject(final String id, final String name, final String spritePath) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.id = id;
        this.name = name;
        this.spritePath = spritePath;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = name;
    }

    public String getSpritePath() {
        return spritePath;
    }

    public final void setSpritePath(final String spritePath) {
        this.spritePath = spritePath;
    }
}
