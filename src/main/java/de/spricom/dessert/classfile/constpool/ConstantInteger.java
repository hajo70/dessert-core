package de.spricom.dessert.classfile.constpool;

class ConstantInteger extends ConstantPoolEntry implements ConstantValue<Integer> {
	public static final int TAG = 3;
	private final int value;

	public ConstantInteger(int value) {
		this.value = value;
	}

	@Override
	public String dump() {
		return Integer.toString(value);
	}

	public Integer getValue() {
		return value;
	}
}
