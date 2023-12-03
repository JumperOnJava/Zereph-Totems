package io.github.jumperonjava.zereph_totems;

public interface Furry {
    default void setFurryState(FurryState state){}
    default FurryState getFurryState(){return FurryState.HUMAN;}
    default boolean isCustomTexture(){return false;}
    default void setCustomTexture(boolean isCustom){}
}
