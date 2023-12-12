# FocusTimer - JavaFX

This project is a rewrite of https://github.com/songi255/focus-timer/tree/v0.1.0 from Java/FXML into Kotlin with coded layouts.  

The application is a Pomodoro timer.  It has two countdown timers, one called "Focus" and another called "Rest".  The basic idea is that you focus on your work while the "Focus" time is active (usually for around 25 minutes or so), and then take a break during the "Rest" countdown.  While running, the application GUI shrinks down to a small size showing just the disk representing the timer, and the opacity can be reduced so that it doesn't interfere with work on the screen. 

The original project, at https://github.com/songi255/focus-timer/tree/v0.1.0 was posted to the JavaFX community on Reddit, with a request for advice and comment. The Java/FXML project was interesting, and porting it to Kotlin seemed like a good way to understand its architecture in order to comment on it.  So, here you are...

# Programming Notes

## Kotlin 

This project is written in Kotlin because it's just nicer to work with than Java for JavaFX.  Plus, it's always nice to show how much verbose JavaFX boilerplate reduces when integrated with Kotlin.  

## Reactive Design

This project implements JavaFX as a Reactive framework, meaning that the layouts are static but behave dynamically in response to changes in a data representation of state.  This is achieve by binding the properties of the layout Nodes to the properties that comprise the Presentation Model.

## Real-time Updates

The application works in three modes:  Stopped, Running and Paused.  In Stopped and Paused modes, the durations of the timers are static and the end times of the timers moves into the future as each second ticks by.  In Running mode, the end times of the timers are locked in, and the duration of the timers decreases as each second passes.  In Stopped mode, it is possible for the user to alter the durations of the two timers, and this will also update their end times.

Regardless of the mode, the application requires constant updates as time moves on in the real world.  This requires that there is a loop that runs constantly, and which causes values to be updated each time the loop iterates.  For this a JavaFX PauseAnimation is used, which simply waits until a timer rus down, and then invokes a method and restarts.  This is very similar to the way that most real-time games and game engines operate.

While the maximum loop time is about 1 second, which is sufficient to allow the durations and end time displays to update, by dropping down the loop time to 300ms it is possible to eliminate the need to Bind various elements of the Presentation Model to each other to keep them in sync over time.  This also removes the need to re-Bind these elements in different ways according to which mode the application is in.  

The result is just a block of code in the Interactor which is executed once per loop pulse, and that code recalculates values in the presentation mode dependant on the active mode of the application.  Using this approach drastically reduces the complexity of the application.

## GUI Layout 

The layout is fairly simple.  It's a BorderPane with a simulated frame title-bar at the top, the timer disk display in the centre, and the controls in the bottom.  The bottom is just a VBox holding two HBoxes, one of which has the timer values and the other has the Buttons in it.  The timer disk is mostly just two Arcs, one for each timer and in different colours.  Only one is visible at at time.  The Arcs are rotated 90* and their Angle property is connected to the Presentation Model to represent how much time is left.  The "dial" surrounding the Arcs is made of radially oriented lines and Texts.  

There's an animation two switch between the "Mini" view and the "Full" view of the GUI.  This controls both the size and the location of window.  It's possible to have the Full and Mini windows in different locations on the screen.
