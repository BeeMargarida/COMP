.class public inputAsOutput
.super java/lang/Object

.method public static f(I)I
.limit locals 1
.limit stack 0

iload_0
ireturn
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 3
.limit stack 1
iconst_1
invokestatic inputAsOutput/f(I)I

istore_1


iload_1
invokestatic io/println(I)V


return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0

return
.end method
