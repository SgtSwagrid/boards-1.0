# strategy-bots
This project is superseded by: https://github.com/SgtSwagrid/Boards

This repository is an ongoing project featuring:
1. Java implementations for various abstract strategy board games. (`src/strategybots/games/`)
2. Bots created to optimally play and win these games. (`src/strategybots/bots/`)

<p float="left">
  <img src="/img/DotsAndBoxes.PNG" width="200" />
  <img src="/img/Pentago.PNG" width="200" /> 
  <img src="/img/Reversi.PNG" width="200" />
</p>

For some unfortunate reason, most AI courses don't incorporate game bot tournaments into their curricula. If anybody wants to do something about fixing this, we'd be more than happy to get involved.

### Featured bots:
#### Connect Four:
* SwagC4 (negamax + alpha-beta + heuristic)
* TipMCTS (monte-carlo tree search)
#### Tic-Tac-Toe / M,N,K-Game:
* SwagMNK (negamax + alpha-beta + heuristic)
#### Reversi / Othello:
* TipOthello (monte-carlo tree search)

### Featured games:
* [Checkers/Draughts](https://en.wikipedia.org/wiki/English_draughts)
* [Chess](https://en.wikipedia.org/wiki/Chess) (In Progress)
* [Chomp](https://en.wikipedia.org/wiki/Chomp)
* [Clobber](https://en.wikipedia.org/wiki/Clobber)
* [Connect Four](https://en.wikipedia.org/wiki/Connect_Four)
* [Dots and Boxes](https://en.wikipedia.org/wiki/Dots_and_Boxes)
* [Game of the Amazons](https://en.wikipedia.org/wiki/Game_of_the_Amazons)
* [Go](https://en.wikipedia.org/wiki/Go_(game)) (Todo)
* [Pentago](https://en.wikipedia.org/wiki/Pentago)
* [Reversi/Othello](https://en.wikipedia.org/wiki/Reversi)
* [Tic-Tac-Toe/M,N,K-Game](https://en.wikipedia.org/wiki/M,n,k-game)

### Required libraries:
 * [LWJGL 2.9.3](http://legacy.lwjgl.org/)
 * [Slick-Util](http://slick.ninjacave.com/slick-util/)
