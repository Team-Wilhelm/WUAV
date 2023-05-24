package be.interfaces;

import java.util.ArrayList;
import java.util.List;

public abstract class Observable<T> {
    private List<Observer<T>> observers = new ArrayList<>();

    // Add an observer to the list of observers
    public void addObserver(Observer<T> o) {
        observers.add(o);
    }

    // Remove an observer from the list of observers
    public void removeObserver(Observer<T> o) {
        observers.remove(o);
    }

    // Notify all observers with the provided argument
    protected void notifyObservers(T arg) {
        for (Observer<T> o : observers) {
            o.update(this, arg);
        }
    }
}
