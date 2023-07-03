package shadows.placebo.transfer;

public interface PlaceboEnergyStorage {
    int getEnergy();
    int getEnergyCapacity();
    int getMaxInsert();
    int getMaxExtract();

    void setEnergy(int energy);
    void setMaxExtract(int maxExtract);
    void setMaxInsert(int maxInsert);
    void setCapacity(int capacity);
}
