def build(ctx):
  # To allow private interface methods.
  ctx.default_build(javac_source_arg="17",
                    javac_target_arg="17")
