package edu.ucsd.cse110.dogegotchi.doge;

import static edu.ucsd.cse110.dogegotchi.daynightcycle.IDayNightCycleObserver.Period.DAY;
import static edu.ucsd.cse110.dogegotchi.daynightcycle.IDayNightCycleObserver.Period.NIGHT;

import android.os.Handler;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import edu.ucsd.cse110.dogegotchi.daynightcycle.IDayNightCycleObserver;
import edu.ucsd.cse110.dogegotchi.observer.ISubject;
import edu.ucsd.cse110.dogegotchi.ticker.ITickerObserver;

/**
 * Logic for our friendly, sophisticated doge.
 *
 * TODO: Exercise 1 -- add support for {@link State#SLEEPING}. (maybe done?)
 *
 * TODO: Exercise 2 -- enable {@link State#SAD} mood, and add support for {@link State#EATING} behavior. (yeah?)
 */
public class Doge implements ISubject<IDogeObserver>, ITickerObserver, IDayNightCycleObserver {
    /**
     * Current number of ticks. Reset after every potential mood swing.
     */
    int numTicks;
    int eatTicks;

    /**
     * How many ticks before we toss a multi-sided die to check mood swing.
     */
    final int numTicksBeforeMoodSwing;
    final int numTicksDoneEating = 5;

    /**
     * Probability of a mood swing every {@link #numTicksBeforeMoodSwing}.
     */
    final double moodSwingProbability;

    /**
     * State of doge.
     */
    State state;
    private ExecutorService backgroundThreadExecutor = Executors.newSingleThreadExecutor();
    private Future<?> future;


    private Collection<IDogeObserver> observers;

    /**
     * Constructor.
     *
     * @param numTicksBeforeMoodSwing Number of ticks before checking for mood swing.
     * @param moodSwingProbability Probability of a mood swing every {@link #numTicksBeforeMoodSwing}.
     */
    public Doge(final int numTicksBeforeMoodSwing, final double moodSwingProbability) {
        Preconditions.checkArgument(
                0.0 <= moodSwingProbability && moodSwingProbability < 1.0f,
                "Mood swing probability must be in range [0,1).");

        this.numTicks = 0;
        this.numTicksBeforeMoodSwing = numTicksBeforeMoodSwing;
        this.moodSwingProbability = moodSwingProbability;
        this.state = State.HAPPY;
        this.observers = new ArrayList<>();
        Log.i(this.getClass().getSimpleName(), String.format(
                "Creating Doge with initial state %s, with mood swing prob %.2f"
                + "and num ticks before each swing attempt %d",
                this.state, this.moodSwingProbability, this.numTicksBeforeMoodSwing));
    }

    @Override
    public void onTick() {
        this.numTicks++;
        if (this.numTicks > 0
            && (this.numTicks % this.numTicksBeforeMoodSwing) == 0) {
            tryRandomMoodSwing();
            this.numTicks = 0;
        }

        //part 2 eating
        if (this.state == State.EATING) {
            this.eatTicks++;
            if (this.eatTicks == this.numTicksDoneEating) {
                setState(State.HAPPY);
            }
        }
    }

    /**
     * TODO: Exercise 1 -- Fill in this method to randomly make doge sad with probability {@link #moodSwingProbability}. (done?)
     *
     * **Strictly follow** the Finite State Machine in the write-up.
     */
    private void tryRandomMoodSwing() {
        // TODO: Exercise 1 -- Implement this method... (done?)
        if (this.state == State.HAPPY){
            if (moodSwingProbability > Math.random()) {
                setState(State.SAD);
            }
        }
    }
    //IMPLEMENTED THIS BECAUSE WE NEEDED TO IMPLEMENT IDAYNIGHTCYCLEOBSERVER
    @Override
    public void onPeriodChange(Period newPeriod){
        if (newPeriod == DAY) {
            this.setState(State.HAPPY);
        }
        else if (newPeriod == NIGHT && (this.state != State.EATING)){
            this.setState(State.SLEEPING);
        }
        else { // else it is night and doge IS eating
            this.future = backgroundThreadExecutor.submit(() ->{
                try {
                    Thread.sleep((numTicksDoneEating-eatTicks)*1000);
                    this.setState(State.SLEEPING);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    //got to have this method because setState is private
    public void feed(){
        setState(State.EATING);
        eatTicks = 0;
    }


    @Override
    public void register(IDogeObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unregister(IDogeObserver observer) {
        observers.remove(observer);
    }

    /**
     * Updates the state of our friendly doge and updates all observers.
     *
     * Note: observe how by using a setter we guarantee that side effects of
     *       an update occur, namely notifying the observers. And it's unused
     *       right now, hm...
     */
    private void setState(final Doge.State newState) {
        this.state = newState;
        Log.i(this.getClass().getSimpleName(), "Doge state changed to: " + newState);
        for (IDogeObserver observer : this.observers) {
            observer.onStateChange(newState);
        }
    }

    /**
     * Moods and actions for our doge.
     */
    public enum State {
        HAPPY,
        SAD,
        // TODO: Implement asleep and eating states, and transitions between all states.
        SLEEPING,
        EATING;
    }
}
