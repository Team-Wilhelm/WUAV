package be.interfaces;

public interface Observer<T> {
    /**
     * Update the observer with the provided argument
     * @param o The observable object
     * @param arg The argument
     */
    void update(Observable<T> o, T arg);
}
