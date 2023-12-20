package me.sixteen_.sort.api;

@FunctionalInterface
public interface IDelay {

	/**
	 * @return delay in ms
	 */
	int getDelay();

	/**
	 * @return no delay
	 */
	static int defaultDelay() {
		return -1;
	}
}
