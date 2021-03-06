## Raspberry Pi reaction test for Scala eXchange 2015, London [![Codacy Badge](https://api.codacy.com/project/badge/grade/2bdf2ea380b24871b9f52d878005a87b)](https://www.codacy.com/app/krisztian-lachata/raspberrypi-reaction-test)

### Functionality
The application itself is a simple reaction measure system. 
- Start the test with the start button
- Provide your details on the web UI, submit the form
- After the submission it will randomly blink red, green or blue led. You have to push red, green or blue buttons respectively within the configured time range. 
- The progress indicator PWM red led starts from OFF state. Based on your reaction its pulse width will be increased till the point where it reaches its maximum. 
- When the red led reaches its maximum pulse width (brightest) the test is over. Calculated average reaction and number of tries will be recorded and displayed.
- Better reaction time results longer test period -> more tries -> higher score

That is it!
  
### Tool set
- [RaspberryPi](https://www.raspberrypi.org/products/raspberry-pi-2-model-b/) is the meat of the application
- [Fritzing](http://fritzing.org/home/) for circuit design
- [wiringPi](http://wiringpi.com/) for low level GPIO manipulation
- [pi4j](http://pi4j.com/) java library to interact with the pi
- [pi4j-client](http://github.com/lachata/pi4j-client) to be able to run pi4j code remotely on a desktop
- [Akka](http://doc.akka.io/docs/akka/2.4.0/scala.html?_ga=1.247924037.378696074.1444496540) to have actor, persistence, FSM, cluster support
- [AngularJS](https://angularjs.org/) to increase the UI development speed, support push messages
- [Bootstrap](http://getbootstrap.com/) to speed up ergonomical UI development

### How to run
- Build the breadboard
- Clone [pi4j-client](https://github.com/lachatak/pi4j-client) and [runPlaybook.sh](https://github.com/lachatak/pi4j-client/blob/master/remote-server/runPlaybook.sh) after you modified the inventory file to point your raspberry. It will deploy the server to the */home/pi/Development/pi4j-remote-server*
- Start the deployed server with the *start.sh* script
- In this project modify RASPBERRY_PI_IP property in [BaseSettings.scala](project/BaseSettings.scala) to point to your own PI
- Copy [application_template.conf](actor/src/main/resources/application_template.conf) to *application.conf* and modify *MONGO_USER* and *MONGO_PASSWORD* properties + change the host to point your mongo installation
- Start client using *sbt gpioActorTest* command
- The application should be available at [http://localhost:8080](http://localhost:8080)
 
### Circuit layout
Items:
- 4 x 330Ohm resistances
- 1 x red led as a progress indicator
- 1 x RGB led
- 4 x buttons
- Breadboard + wires

GPIO usage
- Start/Stop button -> BCM_25 (input, PinPullResistance.PULL_UP)
- Shutdown button -> BCM_24 (input, PinPullResistance.PULL_UP)
- Red led (RGB bulb) -> BCM_19 (output)
- Green led (RGB bulb) -> BCM_13 (output)
- Blue led (RGB bulb) -> BCM_20 (output)
- Red button -> BCM_21 (input, PinPullResistance.PULL_UP)
- Green button -> BCM_23 (input, PinPullResistance.PULL_UP)
- Blue button -> BCM_24 (input, PinPullResistance.PULL_UP) (during the game it is a reaction button, after the game it is a shutdown button)
- Progress indicator -> BCM_12 (PWM output)

<img src="docs/reaction_bb.jpg" width="800px"/>

<img src="docs/real.jpg" width="600px"/>

### Implementation details
- The test application runs on a laptop usg Akka FSM, persistence, and cluster which controls the Raspberry Pi GPIOs using pi4j-client library
- Low level pi4j native calls are captured by AspectJ and delegated to Raspberry Pi using Akka Cluster

![Alt text](docs/architecture.png?raw=true "Architecture")

