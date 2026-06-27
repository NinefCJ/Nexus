#include <gtest/gtest.h>
#include "parser.hpp"
#include "tokenizer.hpp"

TEST(ParserTest, SimpleCommand) {
    mcmd::Tokenizer tokenizer("/give @p stone 10");
    auto tokens = tokenizer.tokenize();

    mcmd::Parser parser(tokens);
    auto ast = parser.parse();

    ASSERT_TRUE(ast != nullptr);
    EXPECT_EQ(ast->type, mcmd::NodeType::Root);
    ASSERT_EQ(ast->children.size(), 1);

    auto& cmd = ast->children[0];
    EXPECT_EQ(cmd->type, mcmd::NodeType::Command);
    EXPECT_EQ(cmd->value, "give");
    EXPECT_EQ(cmd->children.size(), 3);
}

TEST(ParserTest, MultipleCommands) {
    mcmd::Tokenizer tokenizer("/give @p stone /tp @s @r");
    auto tokens = tokenizer.tokenize();

    mcmd::Parser parser(tokens);
    auto ast = parser.parse();

    ASSERT_TRUE(ast != nullptr);
    EXPECT_EQ(ast->children.size(), 2);
}
