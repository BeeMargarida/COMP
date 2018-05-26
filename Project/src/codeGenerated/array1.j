.class public array1
.super java/lang/Object

.method public static print_array(I)V
.limit locals 3
.limit stack 3
null
istore_1

iconst_0
istore_2

loop0:

iload_2
iload_0
if_icmpge loop0_end

iload_2
null
istore_1

iload_2
iconst_1
iadd
istore_2

goto loop0

loop0_end:

iconst_0
istore_2

loop1:

iload_2
iload_0
if_icmpge loop1_end

null
istore_6

iload 6
iload_2
iconst_1
iadd
istore_2

goto loop1

loop1_end:


return
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 0
.limit stack 1
bipush 10
invokestatic array1/print_array(I)V


return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0
return
.end method
