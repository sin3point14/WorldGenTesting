/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.console.ui;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleColors;
import org.terasology.logic.console.CoreMessageType;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.commandSystem.ConsoleCommand;
import org.terasology.logic.console.commandSystem.exceptions.CommandSuggestionException;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.naming.Name;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.FontColor;
import org.terasology.utilities.CamelCaseMatcher;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A text completion engine with cycle-through functionality
 *
 * @author Martin Steiger, Limeth
 */
public class CyclingTabCompletionEngine implements TabCompletionEngine {
    private static final int MAX_CYCLES = 10;
    private final Console console;
    private int selectionIndex;
    private List<String> previousMatches; //Alphabetically ordered list of matches
    private Message previousMessage;
    private Collection<String> commandNames;
    private String query;

    public CyclingTabCompletionEngine(Console console) {
        this.console = console;
    }

    private boolean updateCommandNamesIfNecessary() {
        Collection<ConsoleCommand> commands = console.getCommands();

        if (commandNames != null && commandNames.size() == commands.size()) {
            return false;
        }

        commandNames = Collections2.transform(commands, new Function<ConsoleCommand, String>() {
            @Override
            public String apply(ConsoleCommand input) {
                return input.getName().toString();
            }
        });
        return true;
    }

    private Set<String> findMatches(Name commandName, List<String> commandParameters,
                                 ConsoleCommand command, int suggestedIndex) {
        if (suggestedIndex <= 0) {
            updateCommandNamesIfNecessary();
            return CamelCaseMatcher.getMatches(commandName.toString(), commandNames, true);
        } else if (command == null) {
            return null;
        }

        List<String> finishedParameters = Lists.newArrayList();

        for (int i = 0; i < suggestedIndex - 1; i++) {
            finishedParameters.add(commandParameters.get(i));
        }

        String currentValue = commandParameters.size() >= suggestedIndex ? commandParameters.get(suggestedIndex - 1) : null;
        EntityRef sender = CoreRegistry.get(LocalPlayer.class).getClientEntity();

        try {
            return command.suggest(currentValue, finishedParameters, sender);
        } catch (CommandSuggestionException e) {
            String causeMessage = e.getLocalizedMessage();

            e.printStackTrace();

            if (causeMessage == null) {
                Throwable cause = e.getCause();
                causeMessage = cause.getLocalizedMessage();

                if (causeMessage == null || causeMessage.isEmpty()) {
                    causeMessage = cause.toString();

                    if (causeMessage == null || causeMessage.isEmpty()) {
                        return null;
                    }
                }
            }

            console.addMessage("Error when suggesting command: " + causeMessage, CoreMessageType.ERROR);
            return null;
        }
    }

    @Override
    public String complete(String rawCommand) {
        if (rawCommand.length() <= 0) {
            reset();
            previousMessage = new Message("Type 'help' to list all commands.");
            console.addMessage(previousMessage);
            return null;
        } else if (query == null) {
            query = rawCommand;
        }

        String commandNameRaw = console.processCommandName(query);
        Name commandName = new Name(commandNameRaw);
        List<String> commandParameters = console.processParameters(query);
        ConsoleCommand command = console.getCommand(commandName);
        int suggestedIndex = commandParameters.size() + (query.charAt(query.length() - 1) == ' ' ? 1 : 0);
        Set<String> matches = findMatches(commandName, commandParameters, command, suggestedIndex);

        if (matches == null || matches.size() <= 0) {
            return query;
        }

        if (previousMatches == null || !matches.equals(Sets.newHashSet(previousMatches))) {
            reset(false);

            if (matches.size() == 1) {
                return generateResult(matches.iterator().next(), commandName, commandParameters, suggestedIndex);
            }

/*            if (matches.length > MAX_CYCLES) {
                console.addMessage(new Message("Too many hits, please refine your search"));
                return query;
            }*/ //TODO Find out a better way to handle too many results while returning useful information

            previousMatches = Lists.newArrayList(matches);
            Collections.sort(previousMatches);
        }

        StringBuilder matchMessageString = new StringBuilder();

        for (int i = 0; i < previousMatches.size(); i++) {
            if (i > 0) {
                matchMessageString.append(' ');
            }

            String match = previousMatches.get(i);

            if (selectionIndex == i) {
                match = FontColor.getColored(match, ConsoleColors.COMMAND);
            }

            matchMessageString.append(match);
        }

        Message matchMessage = new Message(matchMessageString.toString());
        String suggestion = previousMatches.get(selectionIndex);

        if (previousMessage != null) {
            console.replaceMessage(previousMessage, matchMessage);
        } else {
            console.addMessage(matchMessage);
        }

        previousMessage = matchMessage;
        selectionIndex = (selectionIndex + 1) % previousMatches.size();

        return generateResult(suggestion, commandName, commandParameters, suggestedIndex);
    }

    private String generateResult(String suggestion, Name commandName,
                                  List<String> commandParameters, int suggestedIndex) {
        if (suggestedIndex <= 0) {
            return suggestion;
        } else {
            String result = commandName.toString();

            for (int i = 0; i < suggestedIndex - 1; i++) {
                result += " " + commandParameters.get(i);
            }

            return result + " " + suggestion;
        }
    }

    private void reset(boolean removeQuery) {
        if (previousMessage != null) {
            console.removeMessage(previousMessage);
        }

        if (removeQuery) {
            query = null;
        }

        previousMessage = null;
        previousMatches = null;
        selectionIndex = 0;
    }

    @Override
    public void reset() {
        reset(true);
    }
}
