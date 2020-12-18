# The Iterated Prisoner’s Dilemma (IPD)

[Prisoner’s dilemma](https://en.wikipedia.org/wiki/Prisoner%27s_dilemma) is one of the most well-known concepts in modern game theory and it   models multiple interactions in nowadays global world, from nuclear negotiations to economic or social scenarios. The iterated prisoner's dilemma is an extension of the general form except the game is repeatedly played by the same participants. An iterated prisoner's dilemma differs from the original concept of a prisoner's dilemma because participants can learn about the behavioral tendencies of their counterparty. 

## IPD tournament
This project implements one of the Axelrod’s  IPD  tournaments, where N ≥ 2 players will play an  one-on-one  iterative  version  of  the  prisoner’s  dilemma in round-robin tournament. Certain number of rounds is played between each two agents, that we denote as a game. The tournament ends when all the agents have played together their respective games and the winner is the agent with highest average payoff after all games were played.

## Usage

JADE (Java  Agent  Development Framework) was used to develop agents. 
Download [jade.jar](https://jade.tilab.com/dl.php?file=JADE-bin-4.5.0.zip) and place it in  the `lib` directory. Create `classes` directory where generated .class files will be stored. Both `lib` and `classes` directories should be in the root directory of the project.

Compile JADE agents with :

```bash
javac -classpath lib\jade.jar -d classes src\agents\*.java src\*.java
```
In this example, we will create one agent for each agent class. Starting agents using the command line: 
```java 
java -cp lib\jade.jar;classes jade.Boot -agents "mainAgent:MainAgent;randomAgent:RandomAgent;spiteful:Spiteful_agent;tftagent:TFT_agent;pavlov:Pavlov_agent"
```

You can start several agents sharing the same class (agents are started with argument (name:class) separated by a semicolon `;`) . Each one will have its name and will operate independently from the others. Together  with  the  players  (agents),  we  also  consider  the  existence  of  a  main  agent (MainAgent)  that behaves  as  a  hub,  receiving  the  adequate  actions  selected  by  each  agent  and  providing them  the  outcomes  along  the  different  rounds. Therefore, he should always be created. GUI will be opened afterwards and you can start the game.

## Agents

Several agents with different strategies are implemented : 
- Random agent : picks move randomly
- TFT agent : cooperates in the first round, and then in the next rounds it does what the opponent did in the previous round all the time
- Spiteful agent : cooperates until the opponent defects, then defects all the time 
- Pavlov agent : cooperates at the first iteration, and whenever both players do the same at the previous round, but it defects when both players behave different at the previous round. Pavlov, also known as win-stay, lose-switch, resembles a common human behavior that keeps the present strategy while winning, or change to another one when losing
