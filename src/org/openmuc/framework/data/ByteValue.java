/*
 * Copyright 2011-13 Fraunhofer ISE
 *
 * This file is part of OpenMUC.
 * For more information visit http://www.openmuc.org
 *
 * OpenMUC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * OpenMUC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with OpenMUC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openmuc.framework.data;

public final class ByteValue implements Value {

	final byte value;

	public ByteValue(byte value) {
		this.value = value;
	}

	@Override
	public double asDouble() {
		return value;
	}

	@Override
	public float asFloat() {
		return value;
	}

	@Override
	public long asLong() {
		return value;
	}

	@Override
	public int asInt() {
		return value;
	}

	@Override
	public short asShort() {
		return value;
	}

	@Override
	public byte asByte() {
		return value;
	}

	@Override
	public boolean asBoolean() {
		return (value != 0);
	}

	@Override
	public byte[] asByteArray() {
		return new byte[] { value };
	}

	@Override
	public String toString() {
		return Byte.toString(value);
	}

}
