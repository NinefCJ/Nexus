#include <gtest/gtest.h>
#include "tokenizer.hpp"

TEST(TokenizerTest, BasicCommand) {
    mcmd::Tokenizer tokenizer("/give @p diamond 10");
    auto tokens = tokenizer.tokenize();

    ASSERT_EQ(tokens.size(), 4);
    EXPECT_EQ(tokens[0].type, mcmd::TokenType::Command);
    EXPECT_EQ(tokens[0].value, "give");
    EXPECT_EQ(tokens[1].type, mcmd::TokenType::Selector);
    EXPECT_EQ(tokens[1].value, "@p");
    EXPECT_EQ(tokens[2].type, mcmd::TokenType::ItemId);
    EXPECT_EQ(tokens[2].value, "diamond");
    EXPECT_EQ(tokens[3].type, mcmd::TokenType::Number);
    EXPECT_EQ(tokens[3].value, "10");
}

TEST(TokenizerTest, SelectorWithBrackets) {
    mcmd::Tokenizer tokenizer("@e[type=zombie]");
    auto tokens = tokenizer.tokenize();

    ASSERT_EQ(tokens.size(), 1);
    EXPECT_EQ(tokens[0].type, mcmd::TokenType::Selector);
    EXPECT_EQ(tokens[0].value, "@e[type=zombie]");
}

TEST(TokenizerTest, Coordinates) {
    mcmd::Tokenizer tokenizer("~10 ^5 -3");
    auto tokens = tokenizer.tokenize();

    ASSERT_EQ(tokens.size(), 3);
    EXPECT_EQ(tokens[0].type, mcmd::TokenType::Coordinates);
    EXPECT_EQ(tokens[0].value, "~10");
    EXPECT_EQ(tokens[1].type, mcmd::TokenType::Coordinates);
    EXPECT_EQ(tokens[1].value, "^5");
    EXPECT_EQ(tokens[2].type, mcmd::TokenType::Coordinates);
    EXPECT_EQ(tokens[2].value, "-3");
}

TEST(TokenizerTest, ItemId) {
    mcmd::Tokenizer tokenizer("minecraft:diamond_sword");
    auto tokens = tokenizer.tokenize();

    ASSERT_EQ(tokens.size(), 1);
    EXPECT_EQ(tokens[0].type, mcmd::TokenType::ItemId);
    EXPECT_EQ(tokens[0].value, "minecraft:diamond_sword");
}
