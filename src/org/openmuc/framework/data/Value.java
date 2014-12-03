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

public interface Value {

	/**
	 * @throws TypeConversionException
	 */
	public double asDouble();

	/**
	 * @throws TypeConversionException
	 */
	public float asFloat();

	/**
	 * @throws TypeConversionException
	 */
	public long asLong();

	/**
	 * @throws TypeConversionException
	 */
	public int asInt();

	/**
	 * @throws TypeConversionException
	 */
	public short asShort();

	/**
	 * @throws TypeConversionException
	 */
	public byte asByte();

	/**
	 * @throws TypeConversionException
	 */
	public boolean asBoolean();

	public byte[] asByteArray();

}
