package de.spricom.dessert.classfile.constpool;

import java.io.DataInputStream;
import java.io.IOException;

public final class ConstantPool {
	private final ConstantPoolEntry[] entries;
	
	public ConstantPool(DataInputStream is) throws IOException {
		entries = new ConstantPoolEntry[is.readUnsignedShort()];
		int index = 1;
		while (index < entries.length) {
			int tag = is.readUnsignedByte();
			switch (tag) {
			case ConstantUtf8.TAG:
				entries[index] = new ConstantUtf8(is.readUTF());
				break;
			case ConstantInteger.TAG:
				entries[index] = new ConstantInteger(is.readInt());
				break;
			case ConstantFloat.TAG:
				entries[index] = new ConstantFloat(is.readFloat());
				break;
			case ConstantLong.TAG:
				entries[index] = new ConstantLong(is.readLong());
				index++;
				break;
			case ConstantDouble.TAG:
				entries[index] = new ConstantDouble(is.readDouble());
				index++;
				break;
			case ConstantClass.TAG:
				entries[index] = new ConstantClass(is.readUnsignedShort());
				break;
			case ConstantString.TAG:
				entries[index] = new ConstantString(is.readUnsignedShort());
				break;
			case ConstantFieldref.TAG:
				entries[index] = new ConstantFieldref(is.readUnsignedShort(), is.readUnsignedShort());
				break;
			case ConstantMethodref.TAG:
				entries[index] = new ConstantMethodref(is.readUnsignedShort(), is.readUnsignedShort());
				break;
			case ConstantInterfaceMethodref.TAG:
				entries[index] = new ConstantInterfaceMethodref(is.readUnsignedShort(), is.readUnsignedShort());
				break;
			case ConstantNameAndType.TAG:
				entries[index] = new ConstantNameAndType(is.readUnsignedShort(), is.readUnsignedShort());
				break;
			case ConstantMethodHandle.TAG:
				entries[index] = new ConstantMethodHandle(is.readUnsignedByte(), is.readUnsignedShort());
				break;
			case ConstantMethodType.TAG:
				entries[index] = new ConstantMethodType(is.readUnsignedShort());
				break;
			case ConstantInvokeDynamic.TAG:
				entries[index] = new ConstantInvokeDynamic(is.readUnsignedShort(), is.readUnsignedShort());
				break;
			default:
				throw new IOException("Unknown constant-pool tag: " + tag);
			}
			index++;
		}
	}

	public ConstantPoolEntry[] getEntries() {
		return entries;
	}

}
