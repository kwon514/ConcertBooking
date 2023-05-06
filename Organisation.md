# Organisation

* Rachel (UPI: rwu050, GitHub Identifier: rwu050)
    * Implemented the aynchronous subscribe / publish concert subscription functionality. 
    * Implemented the SubscribeResource class to handle and store subscriptions.
    * Added required annotations to DTOs related to subscription service.
    * Performed code review to ensure code was well-commented.
* Kevin (UPI: kwon514, GitHub Identifier: kwon514)
    * Implemented the Concert Domain Model which included the Concert, Performer, Seat, and User classes.
    * Implemented mapper classes.
    * Implemented the ConcertResource class.
    * Worked on implementing the seat booking system.
* Sun (UPI: else977, GitHub Identifier: uzji)
    * Implemented the token-based authentication for login functionality.
    * Worked on implementing the seat booking system.
    * Implemented strategies to minimise chance of concurrency errors.
    * Implemented scalability strategy.

GitHub Issues was used to track the progress of the project and discuss among the team members.

Strategy used to minimise chance of concurrency errors in program execution:

* The strategy used was optimistic concurrency control locking when querying and persisting the seats. Versioning was also implemented to the Seat domain model class.

Domain model organisation:

* The domain model is organised into 5 domain classes: Booking, Concert, Performer, Seat, and User. These 5 classes were sufficient to maintain in the database for our implementation.