# EXIT -- Express Cross-Impact Technique

**EXIT** is a Java program that performs 
an EXIT (Express Cross-Impact Technique) style
cross-impact analysis 
on an input file containing a cross-impact matrix 
which describes the direct impacts between variables.

The main output of the EXIT calculation
is a new cross-impact matrix which describes 
the summed direct and indirect impacts
that are mined in the EXIT calculation process
on the basis of the input matrix.
Certain transformations 
on both input and result matrices
can be helpful in interpreting and analysing the results.

## Usage

    java -jar exit.jar -i inputfile.csv -m maximum_impact_value [options...]

Input file name and the maximum impact value are mandatory arguments.
    
Example:

    java -jar exit.jar -i directimpactmatrix.csv -max 5 -c 7 -s 50000 -sep ;
    
Reads the input matrix from file `directimpactmatrix.csv`. 
Sets the maximum impact value to 5.
Sets the input file value separator character to ';'.
Asks to compute the impact of chains of length 7 and shorter fully.
Asks to use a sample of 50000 chains for estimation of impacts of chains longer than 7.
Results are printed to standard output as `-o` option for output file name is not used.

## Options

`-i` (REQUIRED) : Name of the input file that contains the direct impact matrix that is processed.

`-m` (REQUIRED) : Maximum value allowed in the impact matrix. 
This value is used in the EXIT calculation to compute the relative impacts of impact chains. 
See EXIT method section for details.

`-sep` (OPTIONAL) : The separator character used in the input file that contains the direct impact matrix.
If value is not provided, ';' is used as the default separator character.

`-o` (OPTIONAL) : Output file name : If set, results will be printed to a file whose name is set after the option `-o`. 
Otherwise the results are printed in standard output.
  
`-c` (OPTIONAL) : Greatest length of chains that are computed fully. 
If value is not provided, a sensible full computation length value will be determined on the basis of matrix properties.

`-s` (OPTIONAL) : Sample size for estimation of the relative impact of chains whose length exceeds the full computation length. 
If value is not provided, estimation of the relative impact of chains of any specific length 
between any two variables in the matrix 
will be based on a sample of 1000000.





## Input file

The input file should contain an impact matrix that describes 
the direct impacts between the variables included in the cross-impact analysis.
The file should have as many rows as there are variables.
Each row should have the variable name 
followed by the impacts of that variable on all other variables, 
all data separated by the separator character.
This means that each row should, in addition to the variable name, 
have as many impacts as there are variables (or rows) in the input file.
Empty rows in the file will be ignored.

### Example of a valid input file

        V1;0;0;-1;-4;-3;5;-2;1;0;-3;0;-1
        V2;1;0;-1;3;-4;-3;-1;-4;-3;-2;5;-3
        V3;-4;-1;0;-1;-5;-4;1;1;-2;-1;1;-2
        V4;-4;4;0;0;2;-5;-2;3;1;-3;-5;3
        V5;3;5;-1;4;0;-2;4;-3;5;5;-3;3
        V6;-3;2;0;-2;-3;0;2;5;-4;-3;1;5
        V7;5;3;2;1;-2;-4;0;0;-3;-4;1;0
        V8;-1;-5;0;5;4;0;3;0;-5;5;5;-3
        V9;-2;4;-1;3;-1;4;-1;-1;0;-3;1;-4
        V10;0;1;0;-3;2;-5;0;-3;-3;0;-4;-3
        V11;3;5;3;-5;5;-1;1;2;3;-1;0;-5
        V12;4;-3;-5;0;3;5;-1;5;-4;5;4;0

## Output

As output, the EXIT program prints

1) Computation details (input file name, full computation length, sample size and process duration)
2) The unprocessed direct impact matrix (the input matrix)
3) The normalized direct impact matrix
4) A variable classification based on the direct impacts
5) The summed impact matrix, whose values represent the total impacts between variables of the matrix
6) The normalized summed impact matrix, that can be compared to the normalized direct impact matrix 
to see how the relationships between variables have changed as the indirect impacts are accounted for
7) The difference matrix of normalized direct impact matrix and normalized summed impact matrix
				
The normalization of impact matrices is done by dividing the matrix entry values 
by the mean of absolute impact values. 
After this transformation, the unit of the values in the normalized cross-impact matrix 
will be a "cross-impact unit", the average impact of an average variable on another average variable.
				
## EXIT method




## Interpretation of results






