package com.nolanlawson.japanesenamegenerator.v3.util;

/**
 *
 * @author nolan
 */
public class Pair<E,T> {

    private E first;
    private T second;

    public Pair(E first, T second) {
        this.first = first;
        this.second = second;
    }

    public E getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }


    public void setFirst(E first) {
        this.first = first;
    }

    public void setSecond(T second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return "[" + first + "," + second + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair<E, T> other = (Pair<E, T>) obj;
        if (this.first != other.first && (this.first == null || !this.first.equals(other.first))) {
            return false;
        }
        if (this.second != other.second && (this.second == null || !this.second.equals(other.second))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (this.first != null ? this.first.hashCode() : 0);
        hash = 61 * hash + (this.second != null ? this.second.hashCode() : 0);
        return hash;
    }



}
