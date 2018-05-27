.class public aval8
.super java/lang/Object

.method public static max1()I
.limit locals 4
.limit stack 3
istore_0


istore_1


iload_1
istore_2


iload_0
iload_1
if_icmple loop0_end

iload_0
istore_2


loop0_end:
iconst_2
iconst_4
istore_4


ldc "a"
iload_0
invokestatic aval8/print(Ljava/lang/String;I)V

iload_0
bipush 23
if_icmpge loop1_end

iconst_0
istore_5


goto loop1_next
loop1_end:
iconst_2
iconst_4
istore_5


loop1_next:
iload 5
istore_2



iload_2
ireturn
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 1
.limit stack 1
istore_1


iload_1
invokestatic aval8/println(I)V


return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0
return
.end method
