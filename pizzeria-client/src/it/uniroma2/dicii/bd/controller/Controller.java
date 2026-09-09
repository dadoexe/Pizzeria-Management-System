/**
 * Copyright (C) 2022-2025 Alessandro Pellegrini
 * ---
 * This file is part of the material for the Databases course at
 * Tor Vergata University of Rome.
 * ---
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * ---
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * ---
 * You should have received a copy of the GNU Lesser General Public License
 * along with this source.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.uniroma2.dicii.bd.controller;

import it.uniroma2.dicii.bd.exception.ApplicationException;

public interface Controller {

    void start() throws ApplicationException;
}
