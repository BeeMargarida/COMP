.class public stackSize
.super java/lang/Object

.method public static f(I)V
.limit locals 1
.limit stack 5
iload_0
iload_0
iload_0
iload_0
iload_0
invokestatic stackSize/h(IIII)I

if_icmple loop0_end

ldc "Greater"
invokestatic io/println(Ljava/lang/String;)V

goto loop0_next
loop0_end:
ldc "Not greater"
invokestatic io/println(Ljava/lang/String;)V

loop0_next:

return
.end method


.method public static g(I)I
.limit locals 2
.limit stack 5
iload_0
iload_0
iload_0
iload_0
iload_0
invokestatic stackSize/h(IIII)I

imul
istore_1



iload_1
ireturn
.end method


.method public static h(IIII)I
.limit locals 5
.limit stack 2
iload_0
iload_1
iadd
istore 4


iload 4
iload_2
iadd
istore 4


iload 4
iload_3
iadd
istore 4



iload 4
ireturn
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 2
.limit stack 1
iconst_m1
istore_1


iload_1
invokestatic stackSize/f(I)V


return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0

return
.end method
