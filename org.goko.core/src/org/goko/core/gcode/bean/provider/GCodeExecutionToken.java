/*
 *
 *   Goko
 *   Copyright (C) 2013  PsyKo
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.goko.core.gcode.bean.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.goko.core.common.event.EventDispatcher;
import org.goko.core.common.exception.GkException;
import org.goko.core.gcode.bean.BoundingTuple6b;
import org.goko.core.gcode.bean.GCodeCommand;
import org.goko.core.gcode.bean.GCodeCommandState;
import org.goko.core.gcode.bean.IGCodeProvider;
import org.goko.core.gcode.bean.execution.IGCodeExecutionToken;

/**
 * Implementation of a {@link IGCodeProvider} for execution planner
 *
 * @author PsyKo
 *
 */
public class GCodeExecutionToken  extends EventDispatcher implements IGCodeExecutionToken {
	/** Id of the GCodeProvider */
	private Integer id;
	/** The name of this provider */
	private String name;
	/** The bounds of this provider */
	private BoundingTuple6b bounds;
	/** The map of commands by Id */
	protected Map<Integer, GCodeCommand> mapCommandById;
	/** The map of executed commands */
	protected List<Integer> mapExecutedCommandById;
	/** The map of errors commands */
	protected List<Integer> mapErrorsCommandById;
	/** The list of commands */
	protected List<Integer> commands;
	/** The current command index */
	protected int currentIndex;

	/**
	 * Constructor
	 * @param provider the provider to build this execution token from
	 */
	public GCodeExecutionToken(IGCodeProvider provider) {
		this.name = provider.getName();
		this.mapCommandById 		= new HashMap<Integer, GCodeCommand>();
		this.mapExecutedCommandById = new ArrayList<Integer>();
		this.mapErrorsCommandById 	= new ArrayList<Integer>();
		this.bounds = provider.getBounds();
		this.currentIndex = -1;
		this.commands = new ArrayList<Integer>();

		if(CollectionUtils.isNotEmpty(provider.getGCodeCommands())){
			for (GCodeCommand gCodeCommand : provider.getGCodeCommands()) {
				this.commands.add(gCodeCommand.getId());
				this.mapCommandById.put(gCodeCommand.getId(), gCodeCommand);
			}
		}
	}

	/**
	 * @return the id
	 */
	@Override
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.bean.IGCodeProvider#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.bean.IGCodeProvider#getGCodeCommands()
	 */
	@Override
	public List<GCodeCommand> getGCodeCommands() {
		return new ArrayList<GCodeCommand>(mapCommandById.values());
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.bean.IGCodeProvider#getBounds()
	 */
	@Override
	public BoundingTuple6b getBounds() {
		return bounds;
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.bean.execution.IGCodeExecutionToken#getCommandCount()
	 */
	@Override
	public int getCommandCount() throws GkException {
		return commands.size();
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.bean.execution.IGCodeExecutionToken#markAsExecuted(java.lang.Integer)
	 */
	@Override
	public void markAsExecuted(Integer idCommand) throws GkException {
		GCodeCommand command = mapCommandById.get(idCommand);
		mapExecutedCommandById.add(command.getId());
		notifyListeners(new GCodeCommandExecutionEvent(this, command, GCodeCommandState.EXECUTED));
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.bean.execution.IGCodeExecutionToken#getExecutedCommandCount()
	 */
	@Override
	public int getExecutedCommandCount() throws GkException {
		return mapExecutedCommandById.size();
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.bean.execution.IGCodeExecutionToken#markAsError(java.lang.Integer)
	 */
	@Override
	public void markAsError(Integer idCommand) throws GkException {
		GCodeCommand command = mapCommandById.get(idCommand);
		mapErrorsCommandById.add(command.getId());
		notifyListeners(new GCodeCommandExecutionEvent(this, command, GCodeCommandState.ERROR));
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.bean.execution.IGCodeExecutionToken#getErrorCommandCount()
	 */
	@Override
	public int getErrorCommandCount() throws GkException {
		return mapErrorsCommandById.size();
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.bean.execution.IGCodeExecutionToken#getCommandState(java.lang.Integer)
	 */
	@Override
	public GCodeCommandState getCommandState(Integer idCommand) throws GkException {
		return new GCodeCommandState(GCodeCommandState.NONE);
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.bean.execution.IGCodeExecutionToken#getNextCommand()
	 */
	@Override
	public GCodeCommand getNextCommand() throws GkException {
		return mapCommandById.get(commands.get(currentIndex + 1));
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.bean.execution.IGCodeExecutionToken#takeNextCommand()
	 */
	@Override
	public GCodeCommand takeNextCommand() throws GkException {
		currentIndex = currentIndex + 1;
		Integer nextId = commands.get(currentIndex);
		return mapCommandById.get(nextId);
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.bean.execution.IGCodeExecutionToken#hasMoreCommand()
	 */
	@Override
	public boolean hasMoreCommand() throws GkException {
		return getCommandCount() > currentIndex + 1;
	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.bean.execution.IGCodeExecutionToken#beginExecution()
	 */
	@Override
	public void beginExecution() throws GkException {

	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.bean.execution.IGCodeExecutionToken#endExecution()
	 */
	@Override
	public void endExecution() throws GkException {

	}

	/** (inheritDoc)
	 * @see org.goko.core.gcode.bean.IGCodeProvider#getCommandById(java.lang.Integer)
	 */
	@Override
	public GCodeCommand getCommandById(Integer id) throws GkException {
		return mapCommandById.get(id);
	}



}
