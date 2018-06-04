.class public callMain
.super java/lang/Object

.field static x I
.method public static f()V
.limit locals 0
.limit stack 3
iload_-1
iconst_0
if_icmple loop0_end

getstatic callMain/x I 
iconst_1
isub
putstatic callMain/x I 
invokestatic callMain/main()V

loop0_end:

return
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 1
.limit stack 0
ldc "Call main"
invokestatic io/println(Ljava/lang/String;)V

invokestatic callMain/f()V


return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0

return
.end method
