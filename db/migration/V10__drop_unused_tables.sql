-- The board, reply, board template, board image and coupon features were removed.
-- Drop child tables before the tables they point to.
DROP TABLE IF EXISTS image;
DROP TABLE IF EXISTS reply;
DROP TABLE IF EXISTS board;
DROP TABLE IF EXISTS board_template;
DROP TABLE IF EXISTS user_coupon;
DROP TABLE IF EXISTS coupon;
