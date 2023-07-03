package shadows.placebo.fabric.cap;

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import shadows.placebo.transfer.PlaceboEnergyStorage;
import team.reborn.energy.api.EnergyStorage;

public class FabricEnergyStorage extends SnapshotParticipant<Long> implements PlaceboEnergyStorage, EnergyStorage {
    protected int amount = 0;
    protected int capacity;
    protected int maxInsert, maxExtract;

    @Override
    public int getEnergy() {
        return amount;
    }

    @Override
    public int getEnergyCapacity() {
        return capacity;
    }

    @Override
    public boolean supportsInsertion() {
        return maxInsert > 0;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        long inserted = Math.min(maxInsert, Math.min(maxAmount, capacity - amount));

        if (inserted > 0) {
            updateSnapshots(transaction);
            amount += inserted;
            return inserted;
        }

        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return maxExtract > 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        long extracted = Math.min(maxExtract, Math.min(maxAmount, amount));

        if (extracted > 0) {
            updateSnapshots(transaction);
            amount -= extracted;
            return extracted;
        }

        return 0;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public int getMaxInsert() {
        return maxInsert;
    }

    @Override
    public int getMaxExtract() {
        return maxExtract;
    }

    @Override
    public void setEnergy(int energy) {
        this.amount = energy;
    }

    @Override
    public void setMaxExtract(int maxExtract) {
        this.maxExtract = maxExtract;
    }

    @Override
    public void setMaxInsert(int maxInsert) {
        this.maxInsert = maxInsert;
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    protected Long createSnapshot() {
        return (long) amount;
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        amount = snapshot.intValue();
    }
}
