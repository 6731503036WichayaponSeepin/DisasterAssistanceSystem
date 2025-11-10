-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 10, 2025 at 05:32 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `disasterassistancesystem`
--

-- --------------------------------------------------------

--
-- Table structure for table `address`
--

CREATE TABLE `address` (
  `id` bigint(20) NOT NULL,
  `more_details` varchar(255) DEFAULT NULL,
  `subdistrict_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `address`
--

INSERT INTO `address` (`id`, `more_details`, `subdistrict_id`) VALUES
(1, 'ตรงข้ามร้านกาแฟส้มตำ', 1);

-- --------------------------------------------------------

--
-- Table structure for table `affiliated_unit`
--

CREATE TABLE `affiliated_unit` (
  `id` bigint(20) NOT NULL,
  `unit_name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `affiliated_unit`
--

INSERT INTO `affiliated_unit` (`id`, `unit_name`) VALUES
(1, 'ชวนลีกภัย');

-- --------------------------------------------------------

--
-- Table structure for table `assistance_case`
--

CREATE TABLE `assistance_case` (
  `id` bigint(20) NOT NULL,
  `reporter_user_id` bigint(20) NOT NULL,
  `reporter_address_id` bigint(20) DEFAULT NULL,
  `assigned_rescue_id` bigint(20) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `severity` enum('HIGH','MEDIUM','LOW') DEFAULT 'LOW',
  `status` enum('NEW','ASSIGNED','DONE') DEFAULT 'NEW',
  `created_at` datetime(6) DEFAULT current_timestamp(6),
  `case_type` enum('SOS','SUSTENANCE') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `assistance_case`
--

INSERT INTO `assistance_case` (`id`, `reporter_user_id`, `reporter_address_id`, `assigned_rescue_id`, `latitude`, `longitude`, `severity`, `status`, `created_at`, `case_type`) VALUES
(2, 6, NULL, NULL, 30.9101, 100.84188, 'MEDIUM', 'NEW', '2025-11-10 23:26:11.000000', 'SOS'),
(3, 6, NULL, NULL, 30.9101, 100.84188, 'MEDIUM', 'NEW', '2025-11-10 23:27:52.000000', 'SOS'),
(4, 6, NULL, NULL, 30.9101, 100.84188, 'MEDIUM', 'NEW', '2025-11-10 23:28:02.000000', 'SOS'),
(5, 6, NULL, NULL, 600.9101, 500.84188, 'LOW', 'NEW', '2025-11-10 23:28:21.000000', 'SOS'),
(6, 4, NULL, NULL, 19.9101, 99.84188, 'MEDIUM', 'NEW', '2025-11-10 23:28:39.000000', 'SOS'),
(7, 4, NULL, NULL, 19.9101, 99.84188, 'MEDIUM', 'NEW', '2025-11-10 23:28:54.000000', 'SOS'),
(8, 4, NULL, NULL, 19.9101, 99.84188, 'MEDIUM', 'NEW', '2025-11-10 23:29:04.000000', 'SOS');

-- --------------------------------------------------------

--
-- Table structure for table `detail`
--

CREATE TABLE `detail` (
  `id` bigint(20) NOT NULL,
  `name` varchar(160) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `detail`
--

INSERT INTO `detail` (`id`, `name`) VALUES
(1, 'adminuser'),
(21, 'นางสาวชลธิชา วัฒนะ'),
(11, 'นางสาวพรทิพย์ ศรีสม'),
(7, 'นางสาวพิมพ์ชนก รุ่งเรือง'),
(27, 'นางสาวรัชดา มีสุข'),
(5, 'นางสาวรัตนา แก้วใส'),
(16, 'นางสาวรัตนา แก้วใส'),
(18, 'นางสาวศศิธร ใจงาม'),
(24, 'นางสาวอัญชัน ดวงดี'),
(13, 'นายชาญชัย คำดี'),
(20, 'นายณัฐวุฒิ มั่งมี'),
(23, 'นายธนวัฒน์ กิตติสุข'),
(28, 'นายปิยะพงษ์ จิตอาสา'),
(29, 'นายปิยะพงษ์ จิตอาสา'),
(14, 'นายภัทรพล มีสุข'),
(22, 'นายภาณุพงศ์ ศรีทอง'),
(25, 'นายภูริทัต สมจิต'),
(19, 'นายวรพล คำแสง'),
(6, 'นายวิทยา ทองดี'),
(3, 'นายสมชาย ใจดี'),
(4, 'นายสมชาย ใจดี'),
(10, 'นายสมชาย ใจดี'),
(9, 'นายสมดี ใจได้'),
(15, 'นายสันติ สุขใจ'),
(12, 'นายสุชาติ มีบุญ'),
(26, 'นายอรรณพ มั่นคง'),
(17, 'นายอาทิตย์ นวลจันทร์'),
(8, 'นายเอกชัย ศรีสมบัติ');

-- --------------------------------------------------------

--
-- Table structure for table `district`
--

CREATE TABLE `district` (
  `id` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `province_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `district`
--

INSERT INTO `district` (`id`, `name`, `province_id`) VALUES
(1, 'เมืองเชียงราย', 1);

-- --------------------------------------------------------

--
-- Table structure for table `location_data`
--

CREATE TABLE `location_data` (
  `id` bigint(20) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `address_id` bigint(20) DEFAULT NULL,
  `confirmed` bit(1) NOT NULL,
  `followed` bit(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `location_data`
--

INSERT INTO `location_data` (`id`, `latitude`, `longitude`, `address_id`, `confirmed`, `followed`) VALUES
(2, 19.91047, 99.84057, 1, b'0', b'0');

-- --------------------------------------------------------

--
-- Table structure for table `postal_code`
--

CREATE TABLE `postal_code` (
  `id` bigint(20) NOT NULL,
  `code` varchar(100) NOT NULL,
  `subdistrict_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `postal_code`
--

INSERT INTO `postal_code` (`id`, `code`, `subdistrict_id`) VALUES
(1, '57000', 1);

-- --------------------------------------------------------

--
-- Table structure for table `province`
--

CREATE TABLE `province` (
  `id` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `province`
--

INSERT INTO `province` (`id`, `name`) VALUES
(1, 'เชียงราย');

-- --------------------------------------------------------

--
-- Table structure for table `rescue`
--

CREATE TABLE `rescue` (
  `id` bigint(20) NOT NULL,
  `rescue_id` varchar(20) NOT NULL,
  `affiliated_unit_id` bigint(20) NOT NULL,
  `detail_id` bigint(20) NOT NULL,
  `rescue_team_id` bigint(20) DEFAULT NULL,
  `role` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `rescue`
--

INSERT INTO `rescue` (`id`, `rescue_id`, `affiliated_unit_id`, `detail_id`, `rescue_team_id`, `role`) VALUES
(2, 'RS-001', 1, 9, NULL, 'RESCUE'),
(3, 'RS-002', 1, 10, NULL, 'RESCUE'),
(4, 'RS-003', 1, 11, NULL, 'RESCUE'),
(5, 'RS-004', 1, 12, NULL, 'RESCUE'),
(6, 'RS-005', 1, 13, NULL, 'RESCUE'),
(7, 'RS-006', 1, 14, NULL, 'RESCUE'),
(8, 'RS-007', 1, 15, NULL, 'RESCUE'),
(9, 'RS-008', 1, 16, NULL, 'RESCUE'),
(10, 'RS-009', 1, 17, NULL, 'RESCUE'),
(11, 'RS-010', 1, 18, NULL, 'RESCUE'),
(12, 'RS-011', 1, 19, NULL, 'RESCUE'),
(13, 'RS-012', 1, 20, NULL, 'RESCUE'),
(14, 'RS-013', 1, 21, NULL, 'RESCUE'),
(15, 'RS-014', 1, 22, NULL, 'RESCUE'),
(16, 'RS-015', 1, 23, NULL, 'RESCUE'),
(17, 'RS-016', 1, 24, NULL, 'RESCUE'),
(18, 'RS-017', 1, 25, NULL, 'RESCUE'),
(19, 'RS-018', 1, 26, NULL, 'RESCUE'),
(20, 'RS-019', 1, 27, NULL, 'RESCUE'),
(21, 'RS-020', 1, 28, NULL, 'RESCUE');

-- --------------------------------------------------------

--
-- Table structure for table `rescue_team`
--

CREATE TABLE `rescue_team` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `team_id` varchar(255) NOT NULL,
  `district_id` bigint(20) DEFAULT NULL,
  `leader_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `subdistrict`
--

CREATE TABLE `subdistrict` (
  `id` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `district_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `subdistrict`
--

INSERT INTO `subdistrict` (`id`, `name`, `district_id`) VALUES
(1, 'รอบเวียง', 1);

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `id` bigint(20) NOT NULL,
  `phone_number` varchar(10) NOT NULL,
  `address_id` bigint(20) DEFAULT NULL,
  `detail_id` bigint(20) NOT NULL,
  `role` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`id`, `phone_number`, `address_id`, `detail_id`, `role`) VALUES
(1, '0888888888', NULL, 1, 'USER'),
(3, '0812345678', 1, 4, 'USER'),
(4, '0823456789', NULL, 5, 'USER'),
(5, '0834567890', NULL, 6, 'USER'),
(6, '0845678901', NULL, 7, 'USER'),
(7, '0856789012', NULL, 8, 'USER');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `address`
--
ALTER TABLE `address`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKn2dtxus7uq6tw0nkq7s0f872e` (`subdistrict_id`);

--
-- Indexes for table `affiliated_unit`
--
ALTER TABLE `affiliated_unit`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_e5dlxpfr66ncfcidi0fg4o3a4` (`unit_name`);

--
-- Indexes for table `assistance_case`
--
ALTER TABLE `assistance_case`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_ac_user` (`reporter_user_id`),
  ADD KEY `fk_ac_address` (`reporter_address_id`),
  ADD KEY `fk_ac_rescue` (`assigned_rescue_id`);

--
-- Indexes for table `detail`
--
ALTER TABLE `detail`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_detail_name` (`name`);

--
-- Indexes for table `district`
--
ALTER TABLE `district`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK276utu38g5lgqeth6pwfm3rw2` (`province_id`);

--
-- Indexes for table `location_data`
--
ALTER TABLE `location_data`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKljvywoek6ls7ee8akx5twkdox` (`address_id`);

--
-- Indexes for table `postal_code`
--
ALTER TABLE `postal_code`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKau5ebm8x4pm4rfotk2uwlb5qc` (`subdistrict_id`);

--
-- Indexes for table `province`
--
ALTER TABLE `province`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `rescue`
--
ALTER TABLE `rescue`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_30rwdqgt2wdxcjwfgjbdxse2y` (`rescue_id`),
  ADD KEY `FKo3bu1aqjy1f7iixfn3vfhy6pk` (`affiliated_unit_id`),
  ADD KEY `FK5w11tmapetu8kun4arwq0n4gx` (`detail_id`),
  ADD KEY `FKbbol1gdbfkxntuy9ngeiub7r2` (`rescue_team_id`);

--
-- Indexes for table `rescue_team`
--
ALTER TABLE `rescue_team`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_fixj4au8yychwb21tiyerccq9` (`team_id`),
  ADD KEY `FK6tb4sspl03p9ds4u2ndtmj840` (`district_id`),
  ADD KEY `FKsfa0cdvju8rybbs4xju26v7es` (`leader_id`);

--
-- Indexes for table `subdistrict`
--
ALTER TABLE `subdistrict`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKk489h4wxjre2y1vuxd57ax62f` (`district_id`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK_4bgmpi98dylab6qdvf9xyaxu4` (`phone_number`),
  ADD KEY `FKddefmvbrws3hvl5t0hnnsv8ox` (`address_id`),
  ADD KEY `FKrenkibn148c005t3c51cblidj` (`detail_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `address`
--
ALTER TABLE `address`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `affiliated_unit`
--
ALTER TABLE `affiliated_unit`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `assistance_case`
--
ALTER TABLE `assistance_case`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `detail`
--
ALTER TABLE `detail`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- AUTO_INCREMENT for table `district`
--
ALTER TABLE `district`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `location_data`
--
ALTER TABLE `location_data`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `postal_code`
--
ALTER TABLE `postal_code`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `province`
--
ALTER TABLE `province`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `rescue`
--
ALTER TABLE `rescue`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT for table `rescue_team`
--
ALTER TABLE `rescue_team`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `subdistrict`
--
ALTER TABLE `subdistrict`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `address`
--
ALTER TABLE `address`
  ADD CONSTRAINT `FKn2dtxus7uq6tw0nkq7s0f872e` FOREIGN KEY (`subdistrict_id`) REFERENCES `subdistrict` (`id`);

--
-- Constraints for table `assistance_case`
--
ALTER TABLE `assistance_case`
  ADD CONSTRAINT `fk_ac_address` FOREIGN KEY (`reporter_address_id`) REFERENCES `address` (`id`),
  ADD CONSTRAINT `fk_ac_rescue` FOREIGN KEY (`assigned_rescue_id`) REFERENCES `rescue` (`id`),
  ADD CONSTRAINT `fk_ac_user` FOREIGN KEY (`reporter_user_id`) REFERENCES `user` (`id`);

--
-- Constraints for table `district`
--
ALTER TABLE `district`
  ADD CONSTRAINT `FK276utu38g5lgqeth6pwfm3rw2` FOREIGN KEY (`province_id`) REFERENCES `province` (`id`);

--
-- Constraints for table `location_data`
--
ALTER TABLE `location_data`
  ADD CONSTRAINT `FKljvywoek6ls7ee8akx5twkdox` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`);

--
-- Constraints for table `postal_code`
--
ALTER TABLE `postal_code`
  ADD CONSTRAINT `FKau5ebm8x4pm4rfotk2uwlb5qc` FOREIGN KEY (`subdistrict_id`) REFERENCES `subdistrict` (`id`);

--
-- Constraints for table `rescue`
--
ALTER TABLE `rescue`
  ADD CONSTRAINT `FK5w11tmapetu8kun4arwq0n4gx` FOREIGN KEY (`detail_id`) REFERENCES `detail` (`id`),
  ADD CONSTRAINT `FKbbol1gdbfkxntuy9ngeiub7r2` FOREIGN KEY (`rescue_team_id`) REFERENCES `rescue_team` (`id`),
  ADD CONSTRAINT `FKo3bu1aqjy1f7iixfn3vfhy6pk` FOREIGN KEY (`affiliated_unit_id`) REFERENCES `affiliated_unit` (`id`);

--
-- Constraints for table `rescue_team`
--
ALTER TABLE `rescue_team`
  ADD CONSTRAINT `FK6tb4sspl03p9ds4u2ndtmj840` FOREIGN KEY (`district_id`) REFERENCES `district` (`id`),
  ADD CONSTRAINT `FKsfa0cdvju8rybbs4xju26v7es` FOREIGN KEY (`leader_id`) REFERENCES `rescue` (`id`);

--
-- Constraints for table `subdistrict`
--
ALTER TABLE `subdistrict`
  ADD CONSTRAINT `FKk489h4wxjre2y1vuxd57ax62f` FOREIGN KEY (`district_id`) REFERENCES `district` (`id`);

--
-- Constraints for table `user`
--
ALTER TABLE `user`
  ADD CONSTRAINT `FKddefmvbrws3hvl5t0hnnsv8ox` FOREIGN KEY (`address_id`) REFERENCES `address` (`id`),
  ADD CONSTRAINT `FKrenkibn148c005t3c51cblidj` FOREIGN KEY (`detail_id`) REFERENCES `detail` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
