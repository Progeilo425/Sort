package me.sixteen_.sort;

import me.sixteen_.sort.api.IConfig;
import me.sixteen_.sort.api.IDelay;
import me.sixteen_.sort.api.IOrder;
import me.sixteen_.sort.api.ISort;
import me.sixteen_.sort.api.SortClientModInitializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HopperScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

public class Sort implements ISort, ClientModInitializer {

	private IConfig config;
	private IOrder order;
	private IDelay delay;

	private MinecraftClient mc;
	private ScreenHandler container;

	@Override
	public void onInitializeClient() {
		setConfig(IConfig::defaultConfig);
		setOrder(IOrder::defaultOrder);
		setDelay(IDelay::defaultDelay);

		FabricLoader.getInstance()
				.getEntrypointContainers("sort", SortClientModInitializer.class)
				.forEach(entrypoint -> entrypoint.getEntrypoint().onInitializeSortClient(this));

		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (isContainer(screen)) {
				ScreenKeyboardEvents.afterKeyPress(screen).register(
						(containerScreen, key, scancode, modifiers) -> screenKeyEvent(client, containerScreen, key));
				ScreenMouseEvents.afterMouseClick(screen).register(
						(containerScreen, mouseX, mouseY, button) -> screenKeyEvent(client, containerScreen, button));
			}
		});
	}

	@Override
	public void setConfig(IConfig config) {
		this.config = config;
	}

	@Override
	public void setOrder(IOrder order) {
		this.order = order;
	}

	@Override
	public void setDelay(IDelay delay) {
		this.delay = delay;
	}

	private void screenKeyEvent(MinecraftClient client, Screen containerScreen, int keycode) {
		if (keycode == config.getKeycode()) {
			mc = client;
			ScreenHandler container = ((ScreenHandlerProvider<?>) containerScreen).getScreenHandler();
			this.container = container;
			Thread sortThread = new Thread(this::sort, "sort");
			sortThread.start();
		}
	}

	private boolean isContainer(Screen screen) {
		return screen instanceof GenericContainerScreen ||
				screen instanceof ShulkerBoxScreen ||
				screen instanceof Generic3x3ContainerScreen ||
				screen instanceof HopperScreen;
	}

	private void sort() {
		DefaultedList<Slot> slots = container.slots;
		quicksort(slots, 0, slots.size() - (mc.player.getInventory().size() - 5) - 1);
	}

	private void quicksort(DefaultedList<Slot> slots, int left, int right) {
		if (left >= right || left < 0) {
			return;
		}
		int p = partition(slots, left, right);
		quicksort(slots, left, p - 1);
		quicksort(slots, p + 1, right);
	}

	private int partition(DefaultedList<Slot> slots, int left, int right) {
		Slot pivot = slots.get(right);
		int i = left - 1;

		for (int j = left; j < right; j++) {
			Slot id = slots.get(j);
			if (order.compare(id, pivot) <= 0) {
				i++;
				swap(i, j);
			}
		}
		i++;
		swap(i, right);
		return i;
	}

	private void swap(int i1, int i2) {
		try {
			Thread.sleep(delay.getDelay());
		} catch (Exception e) {
		} finally {
			if (i1 != i2) {
				pickup(i1);
				pickup(i2);
				pickup(i1);
			}
		}
	}

	private void pickup(int i) {
		mc.interactionManager.clickSlot(container.syncId, i, 0, SlotActionType.PICKUP, mc.player);
	}
}