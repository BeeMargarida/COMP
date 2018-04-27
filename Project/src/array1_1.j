.class public aval2
.super java/lang/Object

.method public static f(II)I
.limit locals 2
.limit stack 2
iload_-1
ireturn
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 3
.limit stack 0
iconst_2
iconst_12
invokestatic aval2/f(II)I

istore_0

iload_0
invokestatic aval2/println(I)V

iconst_4
iconst_2
invokestatic aval2/f(II)I

istore_0

iload_0
invokestatic aval2/println(I)V

null
istore_0

iconst_4
iconst_2
invokestatic aval2/f(II)I

istore_0

iload_0
invokestatic aval2/println(I)V

return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0
return
.end method
