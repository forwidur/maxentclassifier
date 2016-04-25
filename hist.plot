clear
reset
set key on
set border 3

set title TITLE

# Add a vertical dotted line at x=0 to show centre (mean) of distribution.
set yzeroaxis

# Each bar is half the (visual) width of its x-range.
set boxwidth 0.025 absolute
set style fill solid 1.0 noborder

bin_width = 0.05;

bin_number(x) = floor(x/bin_width)

rounded(x) = bin_width * ( bin_number(x) + 0.5 )

plot 'hits' using (rounded($1)):(1) smooth frequency title "Hit probs",\
     'misses' using (rounded($1)):(1) smooth frequency with boxes title "Miss probs"