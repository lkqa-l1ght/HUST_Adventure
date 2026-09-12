package hust.adventure.events;
 
public class ExpGainedEvent {
    private final float amount;
 
    public ExpGainedEvent(float amount) {
        this.amount = amount;
    }
 
    public float getAmount() {
        return amount;
    }
}
