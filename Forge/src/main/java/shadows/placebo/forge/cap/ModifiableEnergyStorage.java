package shadows.placebo.forge.cap;

import net.minecraftforge.energy.EnergyStorage;
import shadows.placebo.transfer.PlaceboEnergyStorage;

public class ModifiableEnergyStorage extends EnergyStorage implements PlaceboEnergyStorage {

	public ModifiableEnergyStorage(int capacity) {
		this(capacity, capacity, capacity, 0);
	}

	public ModifiableEnergyStorage(int capacity, int maxTransfer) {
		this(capacity, maxTransfer, maxTransfer, 0);
	}

	public ModifiableEnergyStorage(int capacity, int maxReceive, int maxExtract) {
		this(capacity, maxReceive, maxExtract, 0);
	}

	public ModifiableEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
		super(capacity, maxReceive, maxExtract, energy);
	}

	@Override
	public int getEnergy() {
		return energy;
	}

	@Override
	public int getEnergyCapacity() {
		return capacity;
	}

	@Override
	public int getMaxInsert() {
		return maxReceive;
	}

	@Override
	public int getMaxExtract() {
		return maxExtract;
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public void setTransferRate(int transfer) {
		this.maxExtract = this.maxReceive = transfer;
	}

	public void setMaxExtract(int extract) {
		this.maxExtract = extract;
	}

	@Override
	public void setMaxInsert(int maxInsert) {
		this.maxReceive = maxInsert;
	}

	public void setMaxReceive(int receive) {
		this.maxReceive = receive;
	}

}
