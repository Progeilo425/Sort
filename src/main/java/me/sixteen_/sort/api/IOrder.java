package me.sixteen_.sort.api;

import java.util.Comparator;

import net.minecraft.item.Item;
import net.minecraft.screen.slot.Slot;

@FunctionalInterface
public interface IOrder extends Comparator<Slot> {

	/**
	 * @return order by item id
	 */
	static int defaultOrder(Slot slot1, Slot slot2) {
		int id1 = Item.getRawId(slot1.getStack().getItem());
		int id2 = Item.getRawId(slot2.getStack().getItem());
		id1 = id1 == 0 ? Integer.MAX_VALUE : id1;
		id2 = id2 == 0 ? Integer.MAX_VALUE : id2;
		return Integer.compare(id1, id2);
	}
}