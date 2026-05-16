-- AirNexus - MySQL Database Initialization
-- This runs automatically when the MySQL container first starts

CREATE DATABASE IF NOT EXISTS airnexus_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS airnexus_airline CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS airnexus_flight CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS airnexus_seat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS airnexus_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS airnexus_passenger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS airnexus_payment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS airnexus_notification CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'airnexus_user'@'%' IDENTIFIED BY 'system';


-- Grant all privileges to the app user on all airnexus databases
GRANT ALL PRIVILEGES ON airnexus_auth.* TO 'airnexus_user'@'%';
GRANT ALL PRIVILEGES ON airnexus_airline.* TO 'airnexus_user'@'%';
GRANT ALL PRIVILEGES ON airnexus_flight.* TO 'airnexus_user'@'%';
GRANT ALL PRIVILEGES ON airnexus_seat.* TO 'airnexus_user'@'%';
GRANT ALL PRIVILEGES ON airnexus_booking.* TO 'airnexus_user'@'%';
GRANT ALL PRIVILEGES ON airnexus_passenger.* TO 'airnexus_user'@'%';
GRANT ALL PRIVILEGES ON airnexus_payment.* TO 'airnexus_user'@'%';
GRANT ALL PRIVILEGES ON airnexus_notification.* TO 'airnexus_user'@'%';

FLUSH PRIVILEGES;
